package syntax.backend.runways.entity

import jakarta.persistence.*
import java.util.*
import java.time.LocalDate

@Entity
@Table(name = "courses")
data class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false, length = 30)
    val title: String,

    @ManyToOne
    @JoinColumn(name="maker", referencedColumnName = "id")
    val maker: User,

    @Column(name = "hits", nullable = false, columnDefinition = "jsonb DEFAULT jsonb_build_object(to_char(now(), 'YYYY-MM-DD'), 0)")
    val hits: String = "{}",

    @Convert(converter = StringListConverter::class)
    @Column(name = "bookmark", nullable = false, columnDefinition = "text DEFAULT '{}'::text[]")
    val bookmark: List<String> = emptyList(),

    @Column(name = "distance", nullable = false, columnDefinition = "FLOAT DEFAULT 0")
    val distance: Float = 0.0f,

    @Column(name = "coordinate", columnDefinition = "text")
    val coordinate: String? = null,

    @Column(name = "mapUrl", columnDefinition = "text")
    val mapUrl: String? = null,

    @Column(name = "createdAt", nullable = false)
    val createdAt: LocalDate = LocalDate.now(),

    @Column(name = "updatedAt", nullable = false)
    val updatedAt: LocalDate = LocalDate.now()
)