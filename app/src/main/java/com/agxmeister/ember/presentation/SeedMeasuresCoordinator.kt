package com.agxmeister.ember.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedMeasuresCoordinator @Inject constructor() {
    private val _isPending = MutableStateFlow(false)
    val isPending: StateFlow<Boolean> = _isPending.asStateFlow()

    fun trigger() { _isPending.value = true }
    fun consume() { _isPending.value = false }
}
