package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.RequestCourseIdDTO
import syntax.backend.runways.dto.ResponseCourseDTO
import syntax.backend.runways.dto.ResponseCourseDetailDTO
import syntax.backend.runways.entity.Course
import syntax.backend.runways.service.CourseApiService
import syntax.backend.runways.service.UserApiService
import java.util.UUID

@RestController
@RequestMapping("api/course")
class CourseApiController(
    private val courseApiService: CourseApiService,
    private val userApiService: UserApiService,
) {

    @GetMapping("/list")
    fun getCourseList(@RequestHeader("Authorization") token: String): ResponseEntity<List<ResponseCourseDTO>> {
        val jwtToken = token.substring(7)
        val maker= userApiService.getUserDataFromToken(jwtToken)
        val courses = courseApiService.getCourseList(maker)
        return ResponseEntity.ok(courses)
    }

    @PatchMapping("/update")
    fun updateCourse(@RequestHeader("Authorization") token: String, @RequestParam courseId : UUID, title:String ): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = courseApiService.updateCourse(courseId, title, jwtToken)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{id}")
    fun getCourseById(@PathVariable id: UUID, @RequestHeader("Authorization") token: String): ResponseEntity<ResponseCourseDetailDTO> {
        val jwtToken = token.substring(7)
        val course = courseApiService.getCourseById(id, jwtToken)
        return ResponseEntity.ok(course)
    }

    @DeleteMapping("/delete/{courseId}")
    fun deleteCourse(@RequestHeader("Authorization") token: String, @PathVariable courseId: UUID): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = courseApiService.deleteCourse(courseId, jwtToken)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/bookmark")
    fun addBookmark(@RequestHeader("Authorization") token: String, @RequestBody requestCourseIdDTO: RequestCourseIdDTO): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = courseApiService.addBookmark(requestCourseIdDTO.courseId, jwtToken)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/all")
    fun getAllCourses(@RequestHeader("Authorization") token:String): ResponseEntity<List<ResponseCourseDTO>> {
        val jwtToken = token.substring(7)
        val courses = courseApiService.getAllCourses(jwtToken)
        return ResponseEntity.ok(courses)
    }

    @PatchMapping("/bookmark/remove")
    fun removeBookmark(@RequestHeader("Authorization") token: String, @RequestBody requestCourseIdDTO: RequestCourseIdDTO): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = courseApiService.removeBookmark(requestCourseIdDTO.courseId, jwtToken)
        return ResponseEntity.ok(result)
    }
}