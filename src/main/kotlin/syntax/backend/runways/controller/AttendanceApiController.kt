package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import syntax.backend.runways.dto.AttendanceRequestDTO
import syntax.backend.runways.service.AttendanceApiService

@RestController
@RequestMapping("api/attendance")
class AttendanceApiController (
    private val attendanceApiService: AttendanceApiService,
){
    @PostMapping("/check")
    fun checkAttendance(
        @RequestHeader("Authorization") token: String,
        @RequestBody attendanceRequestDTO: AttendanceRequestDTO
    ): ResponseEntity<Void> {
        val jwtToken = token.substring(7)
        val isChecked = attendanceApiService.checkAttendance(jwtToken, attendanceRequestDTO)
        return if (isChecked) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.badRequest().build()
        }
    }
}