package syntax.backend.runways.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*
import java.time.LocalDateTime

@Entity
@Table(name = "courses")
data class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false, length = 30)
    var title: String,

    @ManyToOne
    @JoinColumn(name = "maker", referencedColumnName = "id")
    val maker: User,

    @JdbcTypeCode(SqlTypes.JSON)
    var bookmark: BookMark = BookMark(),

    @JdbcTypeCode(SqlTypes.JSON)
    var hits: Hits = Hits(),

    @Column(name = "distance", nullable = false)
    val distance: Float = 0.0f,

    @Column(name = "coordinate", columnDefinition = "text")
    val coordinate: String,

    @Column(name = "mapUrl", columnDefinition = "text")
    val mapUrl: String,

    @Column(name = "createdAt", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updatedAt", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CourseStatus = CourseStatus.PUBLIC,
)