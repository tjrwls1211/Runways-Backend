package syntax.backend.runways.entity

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import java.util.*
import java.time.LocalDateTime

@Entity
@Table(name = "courses")
data class Course(
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false, length = 30)
    var title: String,

    @ManyToOne
    @JoinColumn(name = "maker", referencedColumnName = "id")
    val maker: User,

    @JdbcTypeCode(SqlTypes.JSON)
    var hits: Hits = Hits(),

    @Column(name = "distance", nullable = false)
    var distance: Float = 0.0f,

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    var position: Point = GeometryFactory().createPoint(Coordinate(0.0, 0.0)),

    @Column(columnDefinition = "geometry(LineString, 4326)", nullable = false)
    var coordinate: LineString = GeometryFactory().createLineString(arrayOf(Coordinate(0.0, 0.0), Coordinate(1.0, 1.0))),

    @Column(name = "map_url", columnDefinition = "text")
    var mapUrl: String,

    @Column(name = "createdAt", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updatedAt", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CourseStatus = CourseStatus.PUBLIC,

    @Column(name = "usage_count", nullable = false)
    var usageCount : Int = 0,

    @Column(name = "sido", length = 20)
    var sido: String,

    @Column(name = "sigungu", length = 20)
    var sigungu: String,

    @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true,)
    @JsonManagedReference
    var courseTags: MutableList<CourseTag> = mutableListOf()
) {
    override fun toString(): String {
        return "Course(id=$id, title='$title', maker=${maker.id}, status=$status)"
    }
}