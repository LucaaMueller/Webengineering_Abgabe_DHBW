package org.example.web_eng2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.sql.Date;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "buildings")
public class Building {
    @Id
    @JsonProperty("id")
    @Column(name = "id", nullable = false)
    private UUID id = UUID.randomUUID();

    @JsonProperty("name")
    @Column(name = "name", nullable = false)
    private String name;

    @JsonProperty("streetname")
    @Column(name = "streetname", nullable = false)
    private String streetname;

    @JsonProperty("housenumber")
    @Column(name = "housenumber", nullable = false)
    private String housenumber;

    @JsonProperty("country_code")
    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @JsonProperty("postalcode")
    @Column(name = "postalcode", nullable = false)
    private String postalcode;

    @JsonProperty("city")
    @Column(name = "city", nullable = false)
    private String city;

    @JsonProperty("deleted_at")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // Getter und Setter
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreetname() {
        return streetname;
    }

    public void setStreetname(String streetname) {
        this.streetname = streetname;
    }

    public String getHousenumber() {
        return housenumber;
    }

    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setcountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}