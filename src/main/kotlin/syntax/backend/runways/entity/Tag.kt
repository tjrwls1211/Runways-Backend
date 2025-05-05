package syntax.backend.runways.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

@Entity
@Table(name = "tags")
data class Tag(
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, unique = true, length = 50)
    val name: String,

    @Column(name = "usage_count", nullable = false)
    var usageCount: Int = 0,

    @OneToMany(mappedBy = "tag", cascade = [CascadeType.ALL])
    @JsonIgnore
    var courseTags: MutableList<CourseTag> = CopyOnWriteArrayList()

)