package org.iota.access.models.rules

import org.iota.access.models.PolicyAttributeComparable
import org.iota.access.models.PolicyAttributeList
import org.iota.access.models.PolicyAttributeSingle

class LocationRule(
        id: String,
        var latitude: Float,
        var longitude: Float,
        var radius: Float,
        var locationUnit: LocationUnit
) : Rule(id) {
    enum class LocationUnit {
        KILOMETERS, MILES;

        val shortStringValue: String
            get() = when (this) {
                KILOMETERS -> "km"
                MILES -> "mi"
            }

        fun toMeters(value: Float): Float {
            return when (this) {
                MILES -> value * 1.60934f / 1000f
                KILOMETERS -> value / 1000f
            }
        }

        override fun toString(): String = when (this) {
            KILOMETERS -> "kilometers"
            MILES -> "miles"
        }

        companion object {
            fun allValues(): Array<LocationUnit> {
                return arrayOf(KILOMETERS, MILES)
            }
        }
    }

    override fun build(): PolicyAttributeList {
        val first = PolicyAttributeSingle("geolocation", "$latitude,$longitude,${locationUnit.toMeters(radius)}")

        val second = PolicyAttributeSingle("request.geolocation.type", "request.geolocation.value")

        return PolicyAttributeComparable(first, second, PolicyAttributeComparable.Operation.GREATER_OR_EQUAL)
    }
}
