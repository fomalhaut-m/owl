package com.vex.query.criteria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VexSortOrder {
    private String property;
    private String direction = "ASC";

    public boolean checkDesc() {
        return "DESC".equals(direction);
    }

    public static VexSortOrder of(String property) {
        VexSortOrder s = new VexSortOrder();
        s.property = property;
        s.direction = "ASC";
        return s;
    }

    public static VexSortOrder of(String property, String direction) {
        VexSortOrder s = new VexSortOrder();
        s.property = property;
        s.direction = direction;
        return s;
    }

    public static VexSortOrder asc(String property) {
        return of(property, "ASC");
    }

    public static VexSortOrder desc(String property) {
        return of(property, "DESC");
    }

    @Override
    public String toString() {
        return property + " " + direction;
    }
}