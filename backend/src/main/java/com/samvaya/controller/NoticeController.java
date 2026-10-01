package com.samvaya.controller;

import com.samvaya.dto.ApiResponse;
import com.samvaya.dto.DocumentDTO;
import com.samvaya.dto.NoticeDTO;
import com.samvaya.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeDTO>>> getNotices() {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getActiveNotices()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeDTO>> createNotice(@RequestBody NoticeDTO dto) {
        NoticeDTO created = noticeService.createNotice(dto);
        return ResponseEntity.ok(ApiResponse.success("Notice published successfully", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success("Notice deleted successfully", null));
    }

    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<DocumentDTO>>> getDocuments() {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getAllDocuments()));
    }
}
