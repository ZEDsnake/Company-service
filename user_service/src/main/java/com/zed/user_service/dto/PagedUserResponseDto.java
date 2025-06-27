package com.zed.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedUserResponseDto {

    private List<UserResponseDto> users;
    private int page;
    private int totalPages;
    private long totalElements;
}