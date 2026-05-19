package com.vex.query.criteria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VexQueryCriteria {
    private String[] select;
    private String[] exclude;
    private VexCriterion filter;
    private VexSortOrder[] orderBy;
    private VexPageRequest paging;

    public VexQueryCriteria select(String... fields) {
        this.select = fields;
        return this;
    }

    public VexQueryCriteria exclude(String... fields) {
        this.exclude = fields;
        return this;
    }

    public VexQueryCriteria filter(VexCriterion filter) {
        this.filter = filter;
        return this;
    }

    public VexQueryCriteria orderBy(VexSortOrder... sorts) {
        this.orderBy = sorts;
        return this;
    }

    public VexQueryCriteria paging(VexPageRequest paging) {
        this.paging = paging;
        return this;
    }

    public static VexQueryCriteria of() {
        return new VexQueryCriteria();
    }

    public static VexQueryCriteria query(String... fields) {
        VexQueryCriteria q = new VexQueryCriteria();
        q.select = fields;
        return q;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VexQueryCriteria{");
        if (select != null && select.length > 0) {
            sb.append("select=").append(java.util.Arrays.toString(select));
        }
        if (exclude != null && exclude.length > 0) {
            if (sb.length() > 17) sb.append(", ");
            sb.append("exclude=").append(java.util.Arrays.toString(exclude));
        }
        if (filter != null) {
            if (sb.length() > 17) sb.append(", ");
            sb.append("filter=").append(filter);
        }
        if (orderBy != null && orderBy.length > 0) {
            if (sb.length() > 17) sb.append(", ");
            sb.append("orderBy=").append(java.util.Arrays.toString(orderBy));
        }
        if (paging != null) {
            if (sb.length() > 17) sb.append(", ");
            sb.append("paging=").append(paging);
        }
        sb.append("}");
        return sb.toString();
    }
}