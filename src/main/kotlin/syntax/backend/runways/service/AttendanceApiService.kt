package syntax.backend.runways.service

import syntax.backend.runways.dto.AttendanceDTO

interface AttendanceApiService  {
    fun checkAttendance(token: String, attendanceDTO: AttendanceDTO): Boolean
    fun getAttendance(token: String): AttendanceDTO?
    fun updateAttendance(token: String, attendanceDTO: AttendanceDTO): Boolean
}