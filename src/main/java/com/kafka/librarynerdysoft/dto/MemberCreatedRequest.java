package com.kafka.librarynerdysoft.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class MemberCreatedRequest {
    @NotBlank(message = "Name is required")
    private String name;
}
