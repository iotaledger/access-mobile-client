package org.iota.access.models.rules

import org.iota.access.models.PolicyAttributeComparable
import org.iota.access.models.PolicyAttributeList
import org.iota.access.models.PolicyAttributeLogical
import org.iota.access.models.PolicyAttributeSingle
import java.util.*

class TimeRule(
        id: String,
        var fromDate: Date,
        var untilDate: Date
) : Rule(id) {

    override fun build(): PolicyAttributeList {
        val from = PolicyAttributeComparable(
                PolicyAttributeSingle("time", (fromDate.time / 1000).toString()),
                PolicyAttributeSingle("request.time.type", "request.time.value"),
                PolicyAttributeComparable.Operation.LESS_OR_EQUAL
        )

        val until = PolicyAttributeComparable(
                PolicyAttributeSingle("time", (untilDate.time / 1000).toString()),
                PolicyAttributeSingle("request.time.type", "request.time.value"),
                PolicyAttributeComparable.Operation.GREATER_OR_EQUAL
        )

        return PolicyAttributeLogical(listOf(from, until), PolicyAttributeLogical.LogicalOperator.AND)
    }
}
