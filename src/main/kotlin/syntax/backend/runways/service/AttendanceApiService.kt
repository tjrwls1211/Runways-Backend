package syntax.backend.runways.service

import syntax.backend.runways.dto.AttendanceDTO

interface AttendanceApiService  {
    fun checkAttendance(userId: String, attendanceDTO: AttendanceDTO): Boolean
    fun getAttendance(userId: String): AttendanceDTO?
    fun updateAttendance(userId: String, attendanceDTO: AttendanceDTO): Boolean
}