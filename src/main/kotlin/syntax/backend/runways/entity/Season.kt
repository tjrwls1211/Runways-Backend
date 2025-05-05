package syntax.backend.runways.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "season")
data class Season(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 50, unique = true)
    val name: String,

    @Column(nullable = false)
    val startDate: LocalDate,

    @Column(nullable = false)
    val endDate: LocalDate,

    @Column(nullable = false)
    val isActive: Boolean = true // true: 현재 시즌, false: 비활성화된 시즌
)
