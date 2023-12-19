package com.vid.videodownloader.interfaces

interface OnItemClick<T> {
    fun itemClickResult(w: T, name : String)
}