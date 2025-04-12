package syntax.backend.runways.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "course_tags", uniqueConstraints = [UniqueConstraint(columnNames = ["course_id", "tag_id"])])
data class CourseTag(
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    @JsonBackReference
    val course: Course,

    @ManyToOne
    @JoinColumn(name = "tag_id", referencedColumnName = "id")
    val tag: Tag
)