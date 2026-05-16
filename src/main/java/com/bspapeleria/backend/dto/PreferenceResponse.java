package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreferenceResponse {
    private String preferenceId;
    private String sandboxInitPoint;
    private String initPoint;
}