package com.agxmeister.ember.domain.usecase

import com.agxmeister.ember.domain.repository.MeasurementRepository
import javax.inject.Inject

class DeleteMeasurementUseCase @Inject constructor(
    private val repository: MeasurementRepository,
) {
    suspend operator fun invoke(id: Long) {
        repository.delete(id)
    }
}
