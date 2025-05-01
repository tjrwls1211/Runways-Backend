package syntax.backend.runways.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "attendance")
data class Attendance (
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "date", nullable = false)
    val date: LocalDate,

    @Column(name = "body_state", nullable = true)
    var bodyState : String? = null,

    @Column(name = "feeling", nullable = true)
    var feeling : String? = null,

    @Column(name = "course_type_preference", nullable = true)
    var courseTypePreference : String? = null,
)