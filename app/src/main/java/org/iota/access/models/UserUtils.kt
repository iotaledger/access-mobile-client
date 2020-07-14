/*
 *  This file is part of the IOTA Access distribution
 *  (https://github.com/iotaledger/access)
 *
 *  Copyright (c) 2020 IOTA Stiftung.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.iota.access.models

import java.util.*

object UserUtils {
    private val defaultUsers: List<User>
        get() {
            val userList: MutableList<User> = ArrayList()

            userList.add(User(
                    publicId = "3c9d985c5d630e6e02f676997c5e9f03b45c6b7529b2491e8de03c18af3c9d87f0a65ecb5dd8f390dee13835354b222df414104684ce9f1079a059f052ca6e51",
                    username = "alice",
                    firstName = "Alice",
                    lastName = "Smith",
                    walletId = "0xEd3800CCf1Cd24bDb4E23F510FDF4a6D80fb97Cd",
                    signingKey = "Z8Zpc1H/Suwpzbqr8vvjRnzCVPgb6OeNdlouYzOfyZqvzVNEO3kNKu9Fnci+vD2mIk/7kqqAGUxfDmMo4+RJJw=="
            ))

            userList.add(User(
                    publicId = "d51d6d76a6002d34af849e52c22719bd2becfee5b0685acd67b0d16654284cf6d200189d22cdab0aafcfd46b055a2e4f5bde31cae849207fd820e03c32ac21f8",
                    username = "bob",
                    firstName = "Bob",
                    lastName = "Smith",
                    walletId = "0x944c75229a7ADf0995ba4C5aE03199eCf2a1D6A8",
                    signingKey = "Z8Zpc1H/Suwpzbqr8vvjRnzCVPgb6OeNdlouYzOfyZqvzVNEO3kNKu9Fnci+vD2mIk/7kqqAGUxfDmMo4+RJJw=="
            ))

            userList.add(User(
                    publicId = "df2b3fe3be9f6bd3deca19d275eb76b252389c4c4af2ce33745376357c955f1e0c9d1eb776beacd2bd917fc216787e57156edfb8077c816754a10a46b91a81b2",
                    username = "carol",
                    firstName = "Carol",
                    lastName = "Smith",
                    walletId = "0x80f068Eee206dDC9A87f566BB2b3aB151d361022",
                    signingKey = "Z8Zpc1H/Suwpzbqr8vvjRnzCVPgb6OeNdlouYzOfyZqvzVNEO3kNKu9Fnci+vD2mIk/7kqqAGUxfDmMo4+RJJw=="
            ))

            userList.add(User(
                    publicId = "90fb6a83806bc167d68d7eaf21e33fa1e8bb6a32ee536091728461e2655be3769cdc4062942ae87d7ed131e880c63914bf7774f282e3220796ffb30c7f3a6bd8",
                    username = "dave",
                    firstName = "Dave",
                    lastName = "Smith",
                    walletId = "0x9A86e646654C8944572216dbe11a4e0DAF98d5d0",
                    signingKey = "Z8Zpc1H/Suwpzbqr8vvjRnzCVPgb6OeNdlouYzOfyZqvzVNEO3kNKu9Fnci+vD2mIk/7kqqAGUxfDmMo4+RJJw=="
            ))

            return userList
        }

    @JvmStatic
    fun getDefaultUser(username: String): User? {
        for (user in defaultUsers) {
            if (username == user.username) return user
        }
        return null
    }
}
