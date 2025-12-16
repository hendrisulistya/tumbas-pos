package com.argminres.app.domain.usecase.session

import com.argminres.app.data.local.entity.DailySessionEntity
import com.argminres.app.domain.repository.DailySessionRepository

/**
 * Use case to check if there's an active session for today
 */
class SessionCheckUseCase(
    private val dailySessionRepository: DailySessionRepository
) {
    suspend operator fun invoke(): SessionCheckResult {
        val activeSession = dailySessionRepository.getActiveSession()
        
        return if (activeSession != null) {
            SessionCheckResult.SessionActive(activeSession)
        } else {
            SessionCheckResult.NoSession
        }
    }
}

sealed class SessionCheckResult {
    data class SessionActive(val session: DailySessionEntity) : SessionCheckResult()
    object NoSession : SessionCheckResult()
}
