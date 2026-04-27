package com.uob.turbine.query;

import java.util.List;
import java.util.ArrayList;


public class QueryBuilder {
    private final List<QueryFilter> filters = new ArrayList<>();

    public QueryBuilder where(String field, FilterOperator op, Object value) {
        filters.add(new QueryFilter(field, op, value));
        return this;
    }

    public List<QueryFilter> build() {
        return filters;
    }
}

