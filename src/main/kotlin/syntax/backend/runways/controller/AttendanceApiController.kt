package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import syntax.backend.runways.dto.AttendanceDTO
import syntax.backend.runways.service.AttendanceApiService

@RestController
@RequestMapping("api/attendance")
class AttendanceApiController (
    private val attendanceApiService: AttendanceApiService,
){
    @PostMapping("/check")
    fun checkAttendance(
        @RequestHeader("Authorization") token: String,
        @RequestBody attendanceDTO: AttendanceDTO
    ): ResponseEntity<Void> {
        val jwtToken = token.substring(7)
        val isChecked = attendanceApiService.checkAttendance(jwtToken, attendanceDTO)
        return if (isChecked) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/detail")
    fun getAttendanceDetails(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<AttendanceDTO> {
        val jwtToken = token.substring(7)
        val attendanceDetails = attendanceApiService.getAttendance(jwtToken)
        return if (attendanceDetails != null) {
            ResponseEntity.ok(attendanceDetails)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @PatchMapping("/update")
    fun updateAttendance(
        @RequestHeader("Authorization") token: String,
        @RequestBody attendanceDTO: AttendanceDTO
    ): ResponseEntity<Void> {
        val jwtToken = token.substring(7)
        val isUpdated = attendanceApiService.updateAttendance(jwtToken, attendanceDTO)
        return if (isUpdated) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.badRequest().build()
        }
    }


}