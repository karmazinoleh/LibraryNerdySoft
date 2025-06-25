package com.kafka.librarynerdysoft.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class BorrowBookRequest {
    private Long bookId;

    private Long memberId;
}

