package com.botdroid.app

data class Rule(
    var id: String,
    var name: String,
    var keywords: String,
    var greeting: Boolean,
    var enabled: Boolean,
    var replies: MutableMap<String, String>
)
