package com.adam.documentrepositoryapi.models;

import com.adam.documentrepositoryapi.models.Document;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String version;
    private LocalDate dateOfCreation;
    private LocalDate dateOfSubmission = LocalDate.now();
    private String fileName;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    public Version() {
    }

    public Version(String version, LocalDate dateOfCreation, String fileName) {
        this.version = version;
        this.dateOfCreation = dateOfCreation;
        this.fileName = fileName;
    }

    public Version(String version, LocalDate dateOfCreation, LocalDate dateOfSubmission, String fileName, Document document) {
        this.version = version;
        this.dateOfCreation = dateOfCreation;
        this.dateOfSubmission = dateOfSubmission;
        this.fileName = fileName;
        this.document = document;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDate getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(LocalDate dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    public LocalDate getDateOfSubmission() {
        return dateOfSubmission;
    }

    public void setDateOfSubmission(LocalDate dateOfSubmission) {
        this.dateOfSubmission = dateOfSubmission;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public String toString() {
        return "Version{" +
                "id=" + id +
                ", version='" + version + '\'' +
                ", dateOfCreation=" + dateOfCreation +
                ", dateOfSubmission=" + dateOfSubmission +
                ", fileName='" + fileName + '\'' +
                ", document=" + document +
                '}';
    }
}
