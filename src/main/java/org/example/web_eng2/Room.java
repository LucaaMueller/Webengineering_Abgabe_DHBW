package org.example.web_eng2;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "rooms")

public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name")
    private String name;


    @ManyToOne(optional = false)
    @JoinColumn(name = "storey_id", nullable = false, referencedColumnName = "id")
    private Storey storey;

    @Column(name = "deleted_at")
    private Instant deletedAt;


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

    public Storey getStorey() {
        return storey;
    }

    public void setStorey(Storey storey) {
        this.storey = storey;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }


}
