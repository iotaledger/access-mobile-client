package org.iota.access.models.rules

import org.iota.access.models.PolicyAttributeComparable
import org.iota.access.models.PolicyAttributeList
import org.iota.access.models.PolicyAttributeSingle


class ExecuteNumberRule(private val numOfExecutions: Int) : Rule() {
    override fun build(): PolicyAttributeList = PolicyAttributeComparable(
            PolicyAttributeSingle("execution_num", numOfExecutions.toString()),
            PolicyAttributeSingle("request.execution_num.type", "request.execution_num.value"),
            PolicyAttributeComparable.Operation.GREATER_THAN
    )
}
