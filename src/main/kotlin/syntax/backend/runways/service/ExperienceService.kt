package syntax.backend.runways.service

import syntax.backend.runways.entity.User

interface ExperienceService {
    fun addExperience(user: User, experience: Int)
}