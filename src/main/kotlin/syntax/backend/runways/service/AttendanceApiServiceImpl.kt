package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.dto.AttendanceRequestDTO
import syntax.backend.runways.entity.Attendance
import syntax.backend.runways.repository.AttendanceApiRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class AttendanceApiServiceImpl(
    private val attendanceApiRepository: AttendanceApiRepository,
    private val userApiService: UserApiService
) : AttendanceApiService {

    // 출석체크
    override fun checkAttendance(token: String, attendanceRequestDTO: AttendanceRequestDTO): Boolean {
        val user = userApiService.getUserDataFromToken(token)
        val now = LocalDateTime.now()

        // 00:00 ~ 04:29 사이인지 확인
        val isEarlyMorning = now.toLocalTime().isBefore(LocalTime.of(4, 30))

        // 조회할 날짜 설정
        val targetDate = if (isEarlyMorning) LocalDate.now().minusDays(1) else LocalDate.now()

        val existingAttendance = attendanceApiRepository.findByUserAndDate(user, targetDate)

        // 이미 출석체크가 되어 있는 경우
        if (existingAttendance != null) {
            return false
        }

        // 출석체크가 되어 있지 않은 경우
        val attendance = Attendance(
            user = user,
            bodyState = attendanceRequestDTO.bodyState,
            feeling = attendanceRequestDTO.feeling,
            courseTypePreference = attendanceRequestDTO.courseTypePreference,
            date = targetDate
        )
        attendanceApiRepository.save(attendance)
        return true
    }
}