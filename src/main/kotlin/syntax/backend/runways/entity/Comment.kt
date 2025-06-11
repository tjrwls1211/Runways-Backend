package syntax.backend.runways.entity

import jakarta.persistence.*
import java.util.*
import java.time.LocalDateTime

@Entity
@Table(name = "comments")
data class Comment(
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false, referencedColumnName = "id")
    var post : Course,

    @Column(name = "content", nullable = false, columnDefinition = "text")
    val content: String,

    @Column(name = "imageUrl", columnDefinition = "text")
    val imageUrl: String? = null,

    @ManyToOne
    @JoinColumn(name="author", referencedColumnName = "id")
    val author: User,

    @Column(name = "createdAt", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updatedAt", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne
    @JoinColumn(name = "parent", referencedColumnName = "id")
    var parent: Comment? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CommentStatus = CommentStatus.PUBLIC,
)