package org.iota.access.models.rules

import org.iota.access.delegation.RuleSatisfyType
import org.iota.access.models.PolicyAttributeList
import org.iota.access.models.PolicyAttributeLogical


class MultipleRule(
        id: String,
        var ruleList: List<Rule>,
        var ruleSatisfyType: RuleSatisfyType
) : Rule(id) {

    override fun build(): PolicyAttributeList =
            if (ruleList.size == 1) {
                ruleList[0].build()
            } else {
                val policyAttrList: MutableList<PolicyAttributeList> = mutableListOf()
                for (rule in ruleList) {
                    policyAttrList.add(rule.build())
                }
                PolicyAttributeLogical(policyAttrList, ruleSatisfyType.logicalOperation)
            }

}
