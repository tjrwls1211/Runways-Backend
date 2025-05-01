package syntax.backend.runways.service

import syntax.backend.runways.dto.AttendanceRequestDTO

interface AttendanceApiService  {
    fun checkAttendance(token: String, attendanceRequestDTO: AttendanceRequestDTO): Boolean
}