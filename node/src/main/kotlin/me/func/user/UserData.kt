package me.func.user

import java.util.*

data class UserData(
    val id: UUID,

    var lootbox: Int,
    var lootboxOpenned: Int,

    var money: Int,
    var wins: Int,
    var kills: Int,
    var games: Int,
    var music: Boolean,
    var timePlayedTotal: Long,
    var lastEnter: Long,
    var dailyClaimTimestamp: Long,
    var rewardStreak: Int,
    var lastSeenName: String?,
)
