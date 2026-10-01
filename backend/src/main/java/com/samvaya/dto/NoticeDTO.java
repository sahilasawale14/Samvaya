package com.samvaya.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDTO {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String priority;
    private LocalDate publishedDate;
    private LocalDate expiryDate;
    private Boolean isActive;
}
