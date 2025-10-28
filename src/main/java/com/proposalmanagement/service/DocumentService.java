package com.proposalmanagement.service;

import com.proposalmanagement.entity.Document;
import com.proposalmanagement.entity.Project;
import com.proposalmanagement.entity.Proposal;
import com.proposalmanagement.entity.User;
import com.proposalmanagement.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DocumentService {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
    
    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }
    
    public Document createDocument(Document document) {
        return documentRepository.save(document);
    }
    
    public Document updateDocument(Long id, Document documentDetails) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        
        document.setProposal(documentDetails.getProposal());
        document.setProject(documentDetails.getProject());
        document.setFileName(documentDetails.getFileName());
        document.setFilePath(documentDetails.getFilePath());
        document.setFileSize(documentDetails.getFileSize());
        document.setFileType(documentDetails.getFileType());
        document.setDocumentType(documentDetails.getDocumentType());
        document.setDescription(documentDetails.getDescription());
        document.setIsActive(documentDetails.getIsActive());
        
        return documentRepository.save(document);
    }
    
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        documentRepository.delete(document);
    }
    
    public List<Document> getDocumentsByProposal(Proposal proposal) {
        return documentRepository.findByProposal(proposal);
    }
    
    public List<Document> getDocumentsByProposalId(Long proposalId) {
        return documentRepository.findActiveDocumentsByProposalId(proposalId);
    }
    
    public List<Document> getDocumentsByProject(Project project) {
        return documentRepository.findByProject(project);
    }
    
    public List<Document> getDocumentsByProjectId(Long projectId) {
        return documentRepository.findActiveDocumentsByProjectId(projectId);
    }
    
    public List<Document> getDocumentsByUploadedBy(User uploadedBy) {
        return documentRepository.findByUploadedBy(uploadedBy);
    }
    
    public List<Document> getDocumentsByType(Document.DocumentType documentType) {
        return documentRepository.findActiveDocumentsByType(documentType);
    }
    
    public List<Document> getActiveDocuments() {
        return documentRepository.findByIsActiveTrue();
    }
    
    public List<Document> searchDocuments(String keyword) {
        return documentRepository.searchByFileNameOrDescription(keyword);
    }
    
    public Long getDocumentCountByProposal(Long proposalId) {
        return documentRepository.countActiveDocumentsByProposalId(proposalId);
    }
    
    public Long getDocumentCountByProject(Long projectId) {
        return documentRepository.countActiveDocumentsByProjectId(projectId);
    }
    
    public Document activateDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        document.setIsActive(true);
        return documentRepository.save(document);
    }
    
    public Document deactivateDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        document.setIsActive(false);
        return documentRepository.save(document);
    }
    
    public List<Document> uploadMultipleDocuments(List<Document> documents) {
        return documentRepository.saveAll(documents);
    }
    
    public void deleteDocumentsByProposal(Long proposalId) {
        List<Document> documents = documentRepository.findByProposalId(proposalId);
        documents.forEach(doc -> doc.setIsActive(false));
        documentRepository.saveAll(documents);
    }
    
    public void deleteDocumentsByProject(Long projectId) {
        List<Document> documents = documentRepository.findByProjectId(projectId);
        documents.forEach(doc -> doc.setIsActive(false));
        documentRepository.saveAll(documents);
    }
    
    public Document updateDocumentPath(Long id, String newFilePath) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        document.setFilePath(newFilePath);
        return documentRepository.save(document);
    }
    
    public Document updateDocumentSize(Long id, Long fileSize) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        document.setFileSize(fileSize);
        return documentRepository.save(document);
    }
}