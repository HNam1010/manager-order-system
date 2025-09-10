package com.example.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.List;

// DTO tùy chỉnh để trả về thông tin phân trang cần thiết cho frontend
@Data
@NoArgsConstructor
public class PageDTO<T> {
    private List<T> content;
    private int currentPage; // number
    private int pageSize;    // size
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public PageDTO(Page<T> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }
}