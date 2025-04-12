package syntax.backend.runways.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "reports")
data class Report(
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "type", nullable = false, length = 20)
    val type: String,

    @Column(name = "detail_id", nullable = false)
    val detailId: String,

    @Column(name = "content", nullable = false, columnDefinition = "text")
    val content: String
)