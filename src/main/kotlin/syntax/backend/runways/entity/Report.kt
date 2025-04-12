package syntax.backend.runways.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "reports")
data class Report(
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    val type: ReportType,

    @Column(name = "detail_id", nullable = false)
    val detailId: String,

    @Column(name = "content", nullable = false, columnDefinition = "text")
    val content: String
)