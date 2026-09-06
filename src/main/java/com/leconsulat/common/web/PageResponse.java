package com.leconsulat.common.web;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Standard paginated list shape mandated by API_CONTRACT.md §1:
 * { content, totalElements, totalPages, number, size }
 */
public class PageResponse<T> {

    private final List<T> content;
    private final long totalElements;
    private final int totalPages;
    private final int number;
    private final int size;

    public PageResponse(List<T> content, long totalElements, int totalPages, int number, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.number = number;
        this.size = size;
    }

    public static <E, D> PageResponse<D> of(Page<E> page, Function<E, D> mapper) {
        List<D> mapped = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(mapped, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    public static <T> PageResponse<T> ofDto(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    public List<T> getContent() {
        return content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }
}
