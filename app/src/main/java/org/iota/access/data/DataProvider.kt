package org.iota.access.data

import org.iota.access.models.DelegationAction
import org.iota.access.models.DelegationObligation
import org.iota.access.models.DelegationUser


interface DataProvider {

    val deviceId: String

    val availableObligations: List<DelegationObligation>

    val availableActions: List<DelegationAction>

    val availableUsers: List<DelegationUser>
}

class DataProviderImpl : DataProvider {

    override val deviceId: String = "123"

    override val availableObligations: List<DelegationObligation> =
            DataProviderGeneric.getAllObligations()

    override val availableActions: List<DelegationAction> =
            DataProviderGeneric.getAllActions()

    override val availableUsers: List<DelegationUser> =
            DataProviderGeneric.getAllUsers()
}

private object DataProviderGeneric {

    fun getAllObligations(): List<DelegationObligation> = listOf(
            DelegationObligation("Obligation #1", "obligation#1"),
            DelegationObligation("Obligation #2", "obligation#2"),
            DelegationObligation("Obligation #3", "obligation#3")
    )

    fun getAllActions(): List<DelegationAction> = listOf(
            DelegationAction("Action #1", "action#1"),
            DelegationAction("Action #2", "action#2"),
            DelegationAction("Action #3", "action#3"),
            DelegationAction("Action #4", "action#4")
    )

    fun getAllUsers(): List<DelegationUser> = listOf(
            DelegationUser("User #1", "3c9d985c5d630e6e02f676997c5e9f03b45c6b7529b2491e8de03c18af3c9d87f0a65ecb5dd8f390dee13835354b222df414104684ce9f1079a059f052ca6e51"),
            DelegationUser("User #2", "d51d6d76a6002d34af849e52c22719bd2becfee5b0685acd67b0d16654284cf6d200189d22cdab0aafcfd46b055a2e4f5bde31cae849207fd820e03c32ac21f8"),
            DelegationUser("User #3", "df2b3fe3be9f6bd3deca19d275eb76b252389c4c4af2ce33745376357c955f1e0c9d1eb776beacd2bd917fc216787e57156edfb8077c816754a10a46b91a81b2"),
            DelegationUser("User #4", "90fb6a83806bc167d68d7eaf21e33fa1e8bb6a32ee536091728461e2655be3769cdc4062942ae87d7ed131e880c63914bf7774f282e3220796ffb30c7f3a6bd8")
    )
}
