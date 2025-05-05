package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.dto.AttendanceDTO
import syntax.backend.runways.entity.Attendance
import syntax.backend.runways.repository.AttendanceRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class AttendanceApiServiceImpl(
    private val attendanceRepository: AttendanceRepository,
    private val userApiService: UserApiService
) : AttendanceApiService {

    // 출석체크
    override fun checkAttendance(userId: String, attendanceDTO: AttendanceDTO): Boolean {
        val user = userApiService.getUserDataFromId(userId)
        val now = LocalDateTime.now()

        // 00:00 ~ 04:29 사이인지 확인
        val isEarlyMorning = now.toLocalTime().isBefore(LocalTime.of(4, 30))

        // 조회할 날짜 설정
        val targetDate = if (isEarlyMorning) LocalDate.now().minusDays(1) else LocalDate.now()

        val existingAttendance = attendanceRepository.findByUserIdAndDate(userId, targetDate)

        // 이미 출석체크가 되어 있는 경우
        if (existingAttendance != null) {
            return false
        }

        // 출석체크가 되어 있지 않은 경우
        val attendance = Attendance(
            user = user,
            bodyState = attendanceDTO.bodyState,
            feeling = attendanceDTO.feeling,
            courseTypePreference = attendanceDTO.courseTypePreference,
            date = targetDate
        )
        attendanceRepository.save(attendance)
        return true
    }

    // 출석체크 내용 확인
    override fun getAttendance(userId: String): AttendanceDTO? {
        val now = LocalDateTime.now()

        // 00:00 ~ 04:29 사이인지 확인
        val isEarlyMorning = now.toLocalTime().isBefore(LocalTime.of(4, 30))

        // 조회할 날짜 설정
        val targetDate = if (isEarlyMorning) LocalDate.now().minusDays(1) else LocalDate.now()

        val attendance =  attendanceRepository.findByUserIdAndDate(userId, targetDate)

        if (attendance == null) {
            return null
        }

        val attendanceDTO = AttendanceDTO(
            bodyState = attendance.bodyState,
            feeling = attendance.feeling,
            courseTypePreference = attendance.courseTypePreference
        )

        return attendanceDTO
    }

    // 출석체크 수정
    override fun updateAttendance(userId: String, attendanceDTO: AttendanceDTO): Boolean {
        val now = LocalDateTime.now()

        // 00:00 ~ 04:29 사이인지 확인
        val isEarlyMorning = now.toLocalTime().isBefore(LocalTime.of(4, 30))

        // 조회할 날짜 설정
        val targetDate = if (isEarlyMorning) LocalDate.now().minusDays(1) else LocalDate.now()

        val existingAttendance = attendanceRepository.findByUserIdAndDate(userId, targetDate)

        // 출석체크가 되어 있지 않은 경우
        if (existingAttendance == null) {
            return false
        }

        // 출석체크 수정
        existingAttendance.bodyState = attendanceDTO.bodyState
        existingAttendance.feeling = attendanceDTO.feeling
        existingAttendance.courseTypePreference = attendanceDTO.courseTypePreference

        attendanceRepository.save(existingAttendance)
        return true
    }
}