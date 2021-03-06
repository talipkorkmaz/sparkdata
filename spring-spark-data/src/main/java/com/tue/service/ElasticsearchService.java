package com.tue.service;

import com.tue.domain.similarity.CompanySimilarity;
import com.tue.spark.address.AddressComponent;
import com.tue.spark.address.AddressParserDelegator;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.elasticsearch.hadoop.rest.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ElasticsearchService {
    @Autowired
    private JavaSparkContext sc;
    @Autowired
    private SparkSession sparkSession;

    public String handleES(String query) {
        Dataset<Company> companyDataset = ElasticQueryHelper.queryForDataSet(sc, sparkSession, "vnf/companies",
                CompanyQuery.builder()
                        .withQuery("website", "*")
                        .withTerm("name", "global")
                        .build(), Company.class);
        companyDataset.show();

        Dataset<Company> companyDatasetVtown = ElasticQueryHelper.queryForDataSet(sc, sparkSession, "vtown*/companies",
                CompanyQuery.builder()
                        .withQuery("website", "*")
                        .withTerm("name", "global")
                        .build(), Company.class);
        companyDatasetVtown.show();

        Dataset<Row> companyJoined = companyDataset.join(companyDatasetVtown,
                companyDataset.col("website").equalTo(companyDatasetVtown.col("website")));
        companyJoined
                .select(companyDataset.col("name"),
                        companyDataset.col("address.province"),
                        companyDatasetVtown.col("name"),
                        companyDataset.col("website"))
                .show(1000, false);

        return String.format("{\"count\": \"%s-%s => %s\"}", companyDataset.count(), companyDatasetVtown.count(), companyJoined.count());
    }

    public void joinCondition() {
        QueryBuilder companyQuery = CompanyQuery.builder()
                .withQuery("website", "*")
                .withTermKeyword("address.province", "TP Hồ Chí Minh")
                .withTermKeyword("address.district", "Quận Tân Bình")
                .build();
        JavaRDD<Company> companyRdd = ElasticQueryHelper.queryForRDD(sc, "vnf/companies", companyQuery, Company.class);
        JavaRDD<Company> companyRddVtown = ElasticQueryHelper.queryForRDD(sc, "vtown*/companies", CompanyQuery.builder()
                .withQuery("website", "*")
                .withExactQuery("address.address", "quận tân bình")
                .build(), Company.class);

        JavaPairRDD<Company, Company> joined = companyRdd.cache().cartesian(companyRddVtown.cache())
                .filter(tuple2 -> {
                    boolean selected = CompanySimilarity.isSimilar(tuple2._1, tuple2._2);
                    if (selected) {
                        System.out.println(String.format("[%s==%s] <->\n [%s==%s]", tuple2._1.getName(), tuple2._1.getAddress().getAddress(),
                                tuple2._2.getName(), tuple2._2.getAddress().getAddress()));
                    }
                    return selected;
                });
//        ElasticQueryHelper.showData(joined.keys(), sparkSession, Company.class);
        log.info(String.format("{\"count\": \"%s-%s => %s\"}", companyRdd.count(), companyRddVtown.count(), joined.count()));
    }

    public void joinByName() {
        QueryBuilder companyQuery = CompanyQuery.builder()
                .withQuery("website", "*")
                .withTermKeyword("address.province", "TP Hồ Chí Minh")
                .build();
        JavaRDD<Company> companyRdd = ElasticQueryHelper.queryForRDD(sc, "vnf/companies", companyQuery, Company.class);
        JavaRDD<Company> companyRddVtown = ElasticQueryHelper.queryForRDD(sc, "vtown*/companies", CompanyQuery.builder()
                .withQuery("website", "*")
                .build(), Company.class);

        JavaPairRDD<Company, Company> joined = companyRdd.cache().cartesian(companyRddVtown.cache())
                .filter(tuple2 -> {
                    boolean selected = CompanySimilarity.isSimilarByName(tuple2._1, tuple2._2);
                    if (selected) {
                        System.out.println(String.format("[%s==%s] <->\n [%s==%s]", tuple2._1.getName(), tuple2._1.getAddress().getAddress(),
                                tuple2._2.getName(), tuple2._2.getAddress().getAddress()));
                    }
                    return selected;
                });
        log.info(String.format("{\"count\": \"%s-%s => %s\"}", companyRdd.count(), companyRddVtown.count(), joined.count()));
    }

    public void addressVerification() {
        QueryBuilder companyQuery = CompanyQuery.builder()
                .withQuery("website", "*")
                .withTerm("name", "global")
                .build();

        JavaRDD<Company> companyRddVtown = ElasticQueryHelper.queryForRDD(sc, "vnf/companies", companyQuery, Company.class);
        companyRddVtown.collect().forEach(company -> {
            String rawAddress = company.getAddress().getAddress();
            if (rawAddress != null) {
                AddressComponent addressComponent = new AddressParserDelegator().parse(company.getAddress().getAddress());
                System.out.println(String.format("%s [%s]", addressComponent, company.getAddress().getAddress()));
            }
        });
    }
}
