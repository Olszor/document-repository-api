package com.adam.documentrepositoryapi.controllers;

import com.adam.documentrepositoryapi.models.Document;
import com.adam.documentrepositoryapi.models.Version;
import com.adam.documentrepositoryapi.payload.request.DocumentRequest;
import com.adam.documentrepositoryapi.payload.request.VersionRequest;
import com.adam.documentrepositoryapi.payload.response.MessageResponse;
import com.adam.documentrepositoryapi.repository.DocumentRepository;
import com.adam.documentrepositoryapi.repository.VersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Transactional
@RequestMapping("/api/document")
public class DocumentController {

    public static final String DIRECTORY = System.getProperty("user.home") + "/Desktop/uploads/";

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    VersionRepository versionRepository;

    @GetMapping("/get")
    @PreAuthorize("hasRole('ROLE_VIEW_DOCUMENTS')")
    public ResponseEntity<?> getDocuments() {
        return ResponseEntity.ok(documentRepository.findAll());
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_ADD_DOCUMENTS')")
    public ResponseEntity<?> addNewDocument(@RequestParam("name") String documentName, @RequestParam("version") String versionVersion, @RequestParam("dateOfCreation") String dateOfCreationString, @RequestParam("file") MultipartFile file) throws IOException {
        if(documentRepository.getDocumentByName(documentName).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Document with name " + documentName + " already exists!"));
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if(versionRepository.getVersionByFileName(fileName).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: File with name " + fileName + " already exists!"));
        }
        Path fileStorage = get(DIRECTORY, fileName).toAbsolutePath().normalize();
        copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
        LocalDate dateOfCreation = LocalDate.parse(dateOfCreationString);
        Document document = new Document(documentName);
        documentRepository.save(document);
        Version version = new Version(versionVersion, dateOfCreation, fileName);
        version.setDocument(document);
        versionRepository.save(version);
        return ResponseEntity.ok(new MessageResponse("Document added successfully!"));
    }

    @PostMapping("/edit/{documentId}")
    @PreAuthorize("hasRole('ROLE_EDIT_DOCUMENTS')")
    public ResponseEntity<?> editDocument(@RequestBody Document documentRequest, @PathVariable("documentId") Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Document with id " + documentId + " does not exist!"));
        }

        Document document = documentRepository.getDocumentById(documentId).get();
        document.setName(documentRequest.getName());
        return ResponseEntity.ok(new MessageResponse("Document updated successfully!"));
    }

    @DeleteMapping("/delete/{documentId}")
    @PreAuthorize("hasRole('ROLE_DELETE_DOCUMENTS')")
    public ResponseEntity<?> deleteDocument(@PathVariable("documentId") Long documentId) {
        Optional<Document> document = documentRepository.findById(documentId);
        if(!document.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Document with id " + documentId + " does not exist!"));
        }
        document.get().getVersions().forEach(version -> {
            try {
                String fileName = StringUtils.cleanPath(version.getFileName());
                Path fileStorage = get(DIRECTORY, fileName).toAbsolutePath().normalize();
                deleteIfExists(fileStorage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        documentRepository.deleteById(documentId);
        return ResponseEntity.ok(new MessageResponse("Document deleted successfully!"));
    }

    @PostMapping("/add/version/{documentId}")
    @PreAuthorize("hasRole('ROLE_EDIT_DOCUMENTS')")
    public ResponseEntity<?> addDocumentVersion(@RequestParam("version") String versionVersion, @RequestParam("dateOfCreation") String dateOfCreationString, @RequestParam("file") MultipartFile file, @PathVariable("documentId") Long documentId) throws IOException {
        Optional<Document> documentOptional = documentRepository.getDocumentById(documentId);
        if (!documentOptional.isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Document with id " + documentId + " does not exist!"));
        }
        if (versionRepository.getVersionByVersionAndDocument(versionVersion, documentOptional.get()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: This version of this document already exists!"));
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (versionRepository.getVersionByFileName(fileName).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: File with name " + fileName + " already exists!"));
        }
        Path fileStorage = get(DIRECTORY, fileName).toAbsolutePath().normalize();
        copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
        Version version = new Version(versionVersion, LocalDate.parse(dateOfCreationString), fileName);
        version.setDocument(documentOptional.get());
        versionRepository.save(version);
        return ResponseEntity.ok(new MessageResponse("New version added successfully!"));
    }

    @PostMapping("/edit/version/{versionId}")
    @PreAuthorize("hasRole('ROLE_EDIT_DOCUMENTS')")
    public ResponseEntity<?> editDocumentVersion(@RequestBody Version versionRequest, @PathVariable("versionId") Long versionId) {
        if(!versionRepository.existsById(versionId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Version with id " + versionId + " does not exits!"));
        }
        Version version = versionRepository.getById(versionId);
        version.setVersion(versionRequest.getVersion());
        version.setDateOfCreation(versionRequest.getDateOfCreation());
        return ResponseEntity.ok(new MessageResponse("Version updated successfully!"));
    }

    @PostMapping("/edit/version/and/file/{versionId}")
    @PreAuthorize("hasRole('ROLE_EDIT_DOCUMENTS')")
    public ResponseEntity<?> editDocumentVersionAndFile(@RequestParam("version") String versionVersion, @RequestParam("dateOfCreation") String dateOfCreationString, @RequestParam("file") MultipartFile file, @PathVariable("versionId") Long versionId) throws IOException {
        if(!versionRepository.existsById(versionId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Version with id " + versionId + " does not exits!"));
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if(versionRepository.getVersionByFileName(fileName).isPresent() && versionRepository.getVersionByFileName(fileName).get().getId() != versionId) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Other version has file named " + fileName + "!"));
        }
        Path fileStorage = get(DIRECTORY, fileName).toAbsolutePath().normalize();
        copy(file.getInputStream(), fileStorage, REPLACE_EXISTING);
        String replacedFileName = versionRepository.getById(versionId).getFileName();
        Path replacedFileStorage = get(DIRECTORY, replacedFileName).toAbsolutePath().normalize();
        deleteIfExists(replacedFileStorage);
        Version version = versionRepository.getById(versionId);
        version.setVersion(versionVersion);
        version.setDateOfCreation(LocalDate.parse(dateOfCreationString));
        version.setFileName(fileName);
        return ResponseEntity.ok(new MessageResponse("Version updated successfully!"));
    }

    @DeleteMapping("/delete/version/{versionId}")
    @PreAuthorize("hasRole('ROLE_DELETE_DOCUMENTS')")
    public ResponseEntity<?> deleteDocumentVersion(@PathVariable("versionId") Long versionId) throws IOException {
        if(!versionRepository.existsById(versionId)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Version with id " + versionId + " does not exist!"));
        }
        Version version = versionRepository.getById(versionId);
        String fileName = StringUtils.cleanPath(version.getFileName());
        Path fileStorage = get(DIRECTORY, fileName).toAbsolutePath().normalize();
        deleteIfExists(fileStorage);
        versionRepository.deleteById(versionId);
        return ResponseEntity.ok(new MessageResponse("Version deleted successfully!"));
    }

    @GetMapping("download/{fileName}")
    @PreAuthorize("hasRole('ROLE_VIEW_USERS')")
    public ResponseEntity<?> downloadFile(@PathVariable("fileName") String fileName) throws IOException {
        Path filePath = get(DIRECTORY).toAbsolutePath().normalize().resolve(fileName);
        if(!Files.exists(filePath)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: file named " + fileName + " does not exist!"));
        }
        Resource resource = new UrlResource(filePath.toUri());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("File-Name", fileName);
        httpHeaders.add(CONTENT_DISPOSITION, "attachment;File-Name=" + resource.getFilename());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(filePath)))
                .headers(httpHeaders).body(resource);
    }
}
