package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import syntax.backend.runways.entity.Course
import syntax.backend.runways.service.CourseApiService
import syntax.backend.runways.service.UserApiService

@RestController
@RequestMapping("api/course")
class CourseApiController(
    private val courseApiService: CourseApiService,
    private val userApiService: UserApiService,
) {

    @GetMapping("/list")
    fun getCourseList(@RequestHeader("Authorization") token: String): ResponseEntity<List<Course>> {
        val jwtToken = token.substring(7)
        val maker= userApiService.getUserDataFromToken(jwtToken)
        val courses = courseApiService.getCourseList(maker)
        return ResponseEntity.ok(courses)
    }
}