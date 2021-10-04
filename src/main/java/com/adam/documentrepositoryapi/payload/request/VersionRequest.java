package com.adam.documentrepositoryapi.payload.request;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class VersionRequest {

    private Long id;
    private String version;
    private LocalDate dateOfCreation;
    private String fileName;
    private MultipartFile file;

    public VersionRequest() {
    }

    public VersionRequest(String version, LocalDate dateOfCreation, String fileName, MultipartFile file) {
        this.version = version;
        this.dateOfCreation = dateOfCreation;
        this.fileName = fileName;
        this.file = file;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
