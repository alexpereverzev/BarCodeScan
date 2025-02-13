package com.barcode.scan

sealed interface ScanState {

    data object Scanning : ScanState

    data object HandlingResult : ScanState
}