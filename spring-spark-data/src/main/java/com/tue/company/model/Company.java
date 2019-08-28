package com.tue.company.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.Instant;

@Document(indexName = "vnf", type = "companies")
@Getter
@ToString
public class Company {
    @Id
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("name_en")
    private String nameEn;
    @JsonProperty("dob")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][XX]")
    private Instant dob;

    @JsonProperty("tax_id")
    private String taxId;

    @JsonProperty("category")
    private String category;
    @JsonProperty("ceo")
    private String ceo;
    @JsonProperty("ceo_address")
    private String ceoAddress;

    @JsonProperty("website")
    private String website;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("fax")
    private String fax;
    @JsonProperty("email")
    private String email;
    @JsonProperty("mobile")
    private String mobile;
    @JsonProperty("website_ping")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][XX]")
    private Instant websitePing;

    @JsonProperty("description")
    private Description description;
    @JsonProperty("address")
    private Address address;

    @JsonProperty("_geodirectory")
    private GeoDirectory geoDirectory;

    @JsonProperty("_log")
    private LogData logData;

    public String toMultipleLineString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
