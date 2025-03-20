package com.duongw.chatapp.model.base;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    private List<T> content;
    private PageMetadata pageMetadata;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder

    public static class PageMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        PageMetadata metadata = PageMetadata.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageMetadata(metadata)
                .build();
    }
}
