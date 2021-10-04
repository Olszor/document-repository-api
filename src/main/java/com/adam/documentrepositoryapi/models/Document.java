package com.adam.documentrepositoryapi.models;

import javax.persistence.*;
import java.util.List;

@Entity
@Table
public class Document {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    private String name;
    @OneToMany(
            mappedBy = "document",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Version> versions;

    public Document() {
    }

    public Document(String name) {
        this.name = name;
    }

    public Document(String name, List<Version> versions) {
        this.name = name;
        this.versions = versions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", versions=" + versions +
                '}';
    }
}
