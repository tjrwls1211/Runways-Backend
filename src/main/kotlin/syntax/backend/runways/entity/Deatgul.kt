package syntax.backend.runways.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "deatgul")
data class Deatgul (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "postId", nullable = false, columnDefinition = "text")
    val postId: String,

    @Column(name = "content", nullable = false, columnDefinition = "text")
    val content: String,

    @ManyToOne
    @JoinColumn(name="author", referencedColumnName = "id")
    val author: User,

    @Column(name = "createdAt", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updatedAt", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "parent", nullable = false, columnDefinition = "text")
    var parent: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CommentStatus = CommentStatus.PUBLIC,

    @Version
    @Column(name = "version", nullable = false)
    val version: Long = 0
)