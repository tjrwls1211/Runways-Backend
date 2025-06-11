package syntax.backend.runways.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tendency")
data class Tendency (
    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID DEFAULT uuid_generate_v4()")
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "exercise_frequency", nullable = true)
    var exerciseFrequency: String? = null, // 1번 질문: 유산소 운동 빈도

    @Column(name = "running_location", nullable = true)
    var runningLocation: String? = null, // 2번 질문: 달리기 장소

    @Column(name = "running_goal", nullable = true)
    var runningGoal: String? = null, // 3번 질문: 달리기 결과

    @Column(name = "exercise_duration", nullable = true)
    var exerciseDuration: String? = null, // 4번 질문: 운동 시간

    @Column(name = "sleep_duration", nullable = true)
    var sleepDuration: String? = null // 5번 질문: 평균 수면 시간
)