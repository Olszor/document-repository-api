package com.adam.documentrepositoryapi.repository;

import com.adam.documentrepositoryapi.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> getDocumentByName(String name);

    Optional<Document> getDocumentById(Long id);

}
