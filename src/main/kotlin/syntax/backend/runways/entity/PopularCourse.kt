package syntax.backend.runways.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.LocalDate
import java.util.UUID

@Entity
data class PopularCourse(
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "date", nullable = false)
    val date: LocalDate,

    @Column(name = "course_id", nullable = false)
    val courseId: UUID,

    @Column(name = "usage_count", nullable = false)
    val usageCount : Int = 0,
)