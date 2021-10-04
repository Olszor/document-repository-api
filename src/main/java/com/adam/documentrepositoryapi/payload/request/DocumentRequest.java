package com.adam.documentrepositoryapi.payload.request;

import com.adam.documentrepositoryapi.models.Document;
import com.adam.documentrepositoryapi.models.Version;

public class DocumentRequest {

    private Document document;

    private Version version;

    public DocumentRequest() {
    }

    public DocumentRequest(Document document, Version version) {
        this.document = document;
        this.version = version;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version versions) {
        this.version = versions;
    }

    @Override
    public String toString() {
        return "DocumentRequest{" +
                "document=" + document +
                ", versions=" + version +
                '}';
    }
}
