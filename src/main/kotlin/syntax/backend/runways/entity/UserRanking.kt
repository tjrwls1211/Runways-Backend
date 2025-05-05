package syntax.backend.runways.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_ranking",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "season_id"])]
)
data class UserRanking(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    val season: Season,

    @Column(nullable = false)
    var score: Int = 0,

    @Column(nullable = true)
    var rank: Int? = null,

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
