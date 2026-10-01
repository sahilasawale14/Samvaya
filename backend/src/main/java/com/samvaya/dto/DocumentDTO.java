package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String title;
    private String category;
    private String filePath;
    private String fileSize;
    private String description;
    private String visibility;
    private LocalDateTime createdAt;
}
