package com.samvaya.service;

import com.samvaya.dto.DocumentDTO;
import com.samvaya.dto.NoticeDTO;
import com.samvaya.exception.ResourceNotFoundException;
import com.samvaya.model.Document;
import com.samvaya.model.Notice;
import com.samvaya.repository.DocumentRepository;
import com.samvaya.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<NoticeDTO> getActiveNotices() {
        return noticeRepository.findByIsActiveTrueOrderByPublishedDateDesc().stream()
                .map(this::mapToNoticeDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public NoticeDTO createNotice(NoticeDTO dto) {
        Notice notice = Notice.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory() != null ? dto.getCategory() : "GENERAL")
                .priority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM")
                .publishedDate(dto.getPublishedDate() != null ? dto.getPublishedDate() : LocalDate.now())
                .expiryDate(dto.getExpiryDate())
                .isActive(true)
                .build();

        return mapToNoticeDTO(noticeRepository.save(notice));
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with ID: " + noticeId));
        noticeRepository.delete(notice);
    }

    @Transactional(readOnly = true)
    public List<DocumentDTO> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::mapToDocDTO)
                .collect(Collectors.toList());
    }

    private NoticeDTO mapToNoticeDTO(Notice n) {
        return NoticeDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .category(n.getCategory())
                .priority(n.getPriority())
                .publishedDate(n.getPublishedDate())
                .expiryDate(n.getExpiryDate())
                .isActive(n.getIsActive())
                .build();
    }

    private DocumentDTO mapToDocDTO(Document d) {
        return DocumentDTO.builder()
                .id(d.getId())
                .title(d.getTitle())
                .category(d.getCategory())
                .filePath(d.getFilePath())
                .fileSize(d.getFileSize())
                .description(d.getDescription())
                .visibility(d.getVisibility())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
