package org.example.web_eng2;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "storeys")
public class Storey {

    @Id
    @JsonProperty("id")
    @Column(name = "id", nullable = false)
    private UUID id = UUID.randomUUID();


    /*@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;*/

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "building_id", nullable = false, referencedColumnName = "id")
    private Building building;

    @Column(name = "deleted_at")
    private java.time.Instant deletedAt;

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

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public java.time.Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(java.time.Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}