package com.barcode.scan

sealed interface ScanActions {

    data class Camera(
        val number: String
    ) : ScanActions


    data class External(
        val number: String
    ) : ScanActions
}