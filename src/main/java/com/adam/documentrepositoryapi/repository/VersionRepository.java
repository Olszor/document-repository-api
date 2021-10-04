package com.adam.documentrepositoryapi.repository;

import com.adam.documentrepositoryapi.models.Version;
import com.adam.documentrepositoryapi.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {

    List<Version> getAllByDocument(Document document);

    Optional<Version> getVersionByVersionAndDocument(String version, Document document);

    Optional<Version> getVersionByFileName(String fileName);
}
