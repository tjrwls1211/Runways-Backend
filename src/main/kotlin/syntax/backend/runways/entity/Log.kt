package syntax.backend.runways.entity

import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*
import jakarta.persistence.*

@Entity
@Table(name = "logs")
data class Log(
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "createdAt", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "type", nullable = false, length = 32)
    val type: String,

    @Column(name = "ip", nullable = false, length = 64)
    val ip: String,

    @Column(name = "value", nullable = false, columnDefinition = "text")
    val value: String,

    @Column(name = "token", columnDefinition = "text")
    val token: String,

    @ManyToOne
    @JoinColumn(name="user_id", referencedColumnName = "id")
    val user: User? = null,

    @Column(name = "request", columnDefinition = "text")
    val request: String? = null,
)