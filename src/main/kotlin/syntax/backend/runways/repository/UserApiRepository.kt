package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.User


@Repository
interface UserApiRepository : JpaRepository<User, String>{
}