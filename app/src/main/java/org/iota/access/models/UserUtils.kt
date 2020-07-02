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
                    username = "johnsmith",
                    firstName = "John",
                    lastName = "Smith",
                    walletId = "0xa41ea875a26Eef4c4319E9998BEa9333cd1d94Cd",
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
