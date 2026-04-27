package com.uob.turbine.query;

import java.util.List;
import java.util.function.Function;

import org.apache.torque.criteria.Criteria;
import org.apache.torque.criteria.SqlEnum;

public class QueryExecutor {

    public <T> List<T> execute(List<QueryFilter> filters, Class<T> omClass) {
        Criteria torqueCriteria = new Criteria();

        for (QueryFilter filter : filters) {
            SqlEnum operator = filter.getOperator().getSqlEnum();
            Object value = operator == SqlEnum.LIKE
                ? "%" + filter.getValue() + "%"
                : filter.getValue();

            // ✅ Correct argument order: field, operator, value
            torqueCriteria.where(filter.getField(), value, operator);
        }

        Function<Criteria, List<T>> selector = PeerRegistry.getPeerSelector(omClass);
        return selector.apply(torqueCriteria);
    }
}