package syntax.backend.runways.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(length = 24)
    val id: String,

    @Column(length = 30)
    var name: String? = null,

    @Column(length = 255)
    var email: String? = null,

    @Column(nullable = false, length = 6)
    var platform: String,

    @Column(nullable = false, length = 20)
    var role: String = "ROLE_USER",

    var birthdate: LocalDate,

    @Column(length = 6)
    var gender: String? = null,

    @Column(length = 15, unique = true)
    var nickname: String? = null,

    @Column(columnDefinition = "TEXT")
    var profileImageUrl: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    var follow: Follow = Follow(),

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var marketing : Boolean = false,

    @Column(columnDefinition = "TEXT")
    var device : String? = null,

    @Column(columnDefinition = "BOOLEAN")
    var accountPrivate : Boolean = false,
)