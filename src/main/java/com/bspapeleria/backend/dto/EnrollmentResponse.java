package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EnrollmentResponse {
    private Long courseId;
    private String courseTitle;
    private List<Long> completedLessons;
    private Integer progress;
    private Integer currentLessonId;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime enrolledAt;
    private Boolean completed;
    private LocalDateTime completedAt;
    private Boolean certificateUnlocked;
}