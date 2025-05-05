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
import syntax.backend.runways.util.SecurityUtil

@RestController
@RequestMapping("api/attendance")
class AttendanceApiController (
    private val attendanceApiService: AttendanceApiService,
){
    @PostMapping("/check")
    fun checkAttendance(
        @RequestBody attendanceDTO: AttendanceDTO
    ): ResponseEntity<Void> {
        val userId = SecurityUtil.getCurrentUserId()
        val isChecked = attendanceApiService.checkAttendance(userId, attendanceDTO)
        return if (isChecked) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/detail")
    fun getAttendanceDetails(): ResponseEntity<AttendanceDTO> {
        val userId = SecurityUtil.getCurrentUserId()
        val attendanceDetails = attendanceApiService.getAttendance(userId)
        return if (attendanceDetails != null) {
            ResponseEntity.ok(attendanceDetails)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @PatchMapping("/update")
    fun updateAttendance(@RequestBody attendanceDTO: AttendanceDTO): ResponseEntity<Void> {
        val userId = SecurityUtil.getCurrentUserId()
        val isUpdated = attendanceApiService.updateAttendance(userId, attendanceDTO)
        return if (isUpdated) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.badRequest().build()
        }
    }


}