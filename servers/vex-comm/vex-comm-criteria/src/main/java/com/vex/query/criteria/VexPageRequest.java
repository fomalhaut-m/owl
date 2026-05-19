package com.vex.query.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VexPageRequest {
    private int page = 0;
    private int size = 20;

    public static VexPageRequest of(int page, int size) {
        return new VexPageRequest(page, size);
    }

    @Override
    public String toString() {
        return String.format("page=%d, size=%d", page, size);
    }
}