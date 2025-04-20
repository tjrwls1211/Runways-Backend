package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.User


@Repository
interface UserApiRepository : JpaRepository<User, String>{
    fun existsByNickname(nickname: String): Boolean
    fun findByIdIn(ids: List<String>): List<User>
    fun findAllByOrderByExperienceDesc(pageable: Pageable): Page<User>
}