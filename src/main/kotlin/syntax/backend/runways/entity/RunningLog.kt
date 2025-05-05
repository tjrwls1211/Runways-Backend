package syntax.backend.runways.entity

import jakarta.persistence.*
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

    @Column(name = "speed", nullable = false)
    val speed : Float = 0.0f, // 평균 속도 (단위: km/h)

    @Column(name = "start_time", nullable = false)
    val startTime: LocalDateTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalDateTime,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)