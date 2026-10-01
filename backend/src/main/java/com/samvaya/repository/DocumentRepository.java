package com.samvaya.repository;

import com.samvaya.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByVisibilityIn(List<String> visibilities);
    List<Document> findByCategory(String category);
}
