package org.iota.access.models.rules

import org.iota.access.models.PolicyAttributeList
import java.util.*

abstract class Rule(id: String? = null): PolicyAttributeList.Builder {

    var id: String = id ?: UUID.randomUUID().toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rule

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        @JvmStatic
        fun generateId(): String = UUID.randomUUID().toString()
    }
}
