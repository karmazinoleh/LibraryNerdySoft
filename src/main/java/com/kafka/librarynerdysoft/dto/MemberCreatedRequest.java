package com.kafka.librarynerdysoft.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberCreatedRequest {
    @NotBlank(message = "Name is required")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
