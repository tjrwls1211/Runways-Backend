package syntax.backend.runways.entity

import jakarta.persistence.*
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "running_logs")
data class RunningLog(
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = true) // course_id를 nullable로 설정
    val course: Course? = null, // course를 nullable로 설정

    @Column(name = "distance", nullable = false)
    val distance: Float, // 러닝 거리 (단위: km)

    @Column(name = "duration", nullable = false)
    val duration: Long, // 러닝 시간 (단위: 초)

    @Column(name = "avg_speed", nullable = false)
    val avgSpeed: Float = 0.0f, // 평균 속도 (단위: km/h)

    @Column(name = "max_speed", nullable = false)
    val maxSpeed: Float = 0.0f, // 최고 속도 (단위: km/h)

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    var position: Point = GeometryFactory().createPoint(Coordinate(0.0, 0.0)),

    @Column(columnDefinition = "geometry(LineString, 4326)", nullable = false)
    var coordinate: LineString = GeometryFactory().createLineString(arrayOf(Coordinate(0.0, 0.0), Coordinate(1.0, 1.0))),

    @Column(name = "start_time", nullable = false)
    val startTime: LocalDateTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalDateTime,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "map_url", columnDefinition = "text")
    var mapUrl: String,

    @Column(name = "sido", length = 20)
    var sido: String,

    @Column(name = "sigungu", length = 20)
    var sigungu: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: RunningLogStatus = RunningLogStatus.PUBLIC,
)