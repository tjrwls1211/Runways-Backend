package syntax.backend.runity.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(length = 24)
    val id: String,

    @Column(length = 30)
    var name: String,

    @Column(length = 255)
    var email: String,

    @Column(nullable = false, length = 6)
    val platform: String,
    /*
    @Column(nullable = false)
    var birthdate: LocalDate,

    @Column(nullable = false, length = 6)
    var gender: String,

    @Column(nullable = false, length = 15, unique = true)
    var nickname: String,

    @Column(columnDefinition = "TEXT")
    var profileImageUrl: String? = null,

    @Column(columnDefinition = "jsonb")
    var follow: String = """{"followings": [], "followers": []}""",

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
    */
)