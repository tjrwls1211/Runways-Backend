package syntax.backend.runways.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "notifications")
data class Notification (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false, length = 32)
    var title: String,

    @Column(name = "content", nullable = false, length = 255)
    var content: String,

    @Column(name = "type", nullable = false, length = 32)
    val type: String,

    @ManyToOne
    @JoinColumn(name="user_id", referencedColumnName = "id")
    val user: User? = null,

    @Column(nullable = false)
    var read : Boolean = false,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,

)