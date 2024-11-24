package org.example.web_eng2;

import jakarta.persistence.*;


    @Entity
    @Table(name = "buildings")
    public class Building {
        @Id
        @GeneratedValue

        @Column(name = "id")
        private String id;


        @Column(name = "name")
        private String name;

        @Column(name = "streetname")
        private String streetname;

        @Column(name = "housenumber")
        private String housenumber;

        @Column(name = "country_code")
        private String countryCode;

        @Column(name = "postalcode")
        private String postalcode;

        @Column(name = "city")
        private String city;

        @Column(name = "deleted_at")
        private String deletedAt;


}
