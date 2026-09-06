package com.leconsulat.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Builds a Spring {@link Pageable} from the {@code page/size/sort} query params, as per API_CONTRACT.md §1. */
public final class PageableUtil {

    private PageableUtil() {
    }

    public static Pageable build(Integer page, Integer size, String sort) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size <= 0 ? 20 : Math.min(size, 200);
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(p, s);
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(p, s, Sort.by(direction, property));
    }
}
