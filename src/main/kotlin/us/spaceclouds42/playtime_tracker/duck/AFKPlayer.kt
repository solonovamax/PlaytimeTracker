package us.spaceclouds42.playtime_tracker.duck

interface AFKPlayer {
    var isAfk: Boolean
    var playtime: Long
    var lastActionTime: Long
    var lastTickTime: Long
}
