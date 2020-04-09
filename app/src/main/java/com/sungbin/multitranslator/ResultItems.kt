package com.sungbin.multitranslator

class ResultItems {
    var papago: String? = null
    var google: String? = null
    var daum: String? = null

    constructor(){}
    constructor(papago: String?, google: String?, daum: String?) {
        this.papago = papago
        this.google = google
        this.daum = daum
    }
}