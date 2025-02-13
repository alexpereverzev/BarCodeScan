package com.barcode.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {

    private val _stateFlow: MutableStateFlow<ScanState> = MutableStateFlow(ScanState.Scanning)

    val stateFlow: StateFlow<ScanState> = _stateFlow

    private val text = MutableStateFlow("")

    fun action(actions: ScanActions) {
        when (actions) {
            is ScanActions.Camera -> {
                _stateFlow.value = ScanState.HandlingResult
                viewModelScope.launch {
                    handleResult()
                }
            }

            is ScanActions.External -> {
                viewModelScope.launch {
                    text.debounce(2000)
                        .distinctUntilChanged()
                        .collect {
                            _stateFlow.value = ScanState.HandlingResult
                            handleResult()
                        }
                }
            }
        }
    }

    private suspend fun handleResult() {
        // handler result simulation
        delay(1000)
        _stateFlow.value = ScanState.Scanning
    }
}
