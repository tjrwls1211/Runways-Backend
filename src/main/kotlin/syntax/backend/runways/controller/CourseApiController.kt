package syntax.backend.runways.controller

import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.*
import syntax.backend.runways.service.CourseApiService
import syntax.backend.runways.service.UserApiService
import syntax.backend.runways.util.SecurityUtil
import java.util.UUID

@RestController
@RequestMapping("api/course")
class CourseApiController(
    private val courseApiService: CourseApiService,
) {

    @PostMapping("/insert")
    fun createCourse(@RequestBody requestCourseDTO: RequestCourseDTO): ResponseEntity<UUID> {
        val userId = SecurityUtil.getCurrentUserId()
        val newCourseId = courseApiService.createCourse(requestCourseDTO, userId)
        return ResponseEntity.ok(newCourseId)
    }

    @GetMapping("/list")
    fun getCourseList(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<PagedResponse<ResponseCourseDTO>> {
        val pageable = PageRequest.of(page, size)
        val userId = SecurityUtil.getCurrentUserId()
        val courses = courseApiService.getMyCourseList(userId, pageable)

        val pagedResponse = PagedResponse(
            content = courses.content,
            totalPages = courses.totalPages,
            totalElements = courses.totalElements,
            currentPage = courses.number,
            pageSize = courses.size
        )

        return ResponseEntity.ok(pagedResponse)
    }

    @PatchMapping("/update")
    fun updateCourse(@RequestBody requestUpdateCourseDTO: RequestUpdateCourseDTO): ResponseEntity<UUID> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = courseApiService.updateCourse(requestUpdateCourseDTO, userId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{id}")
    fun getCourseById(@PathVariable id: UUID): ResponseEntity<ResponseCourseDetailDTO> {
        val userId = SecurityUtil.getCurrentUserId()
        val course = courseApiService.getCourseById(id, userId)
        return ResponseEntity.ok(course)
    }

    @DeleteMapping("/delete/{courseId}")
    fun deleteCourse(@PathVariable courseId: UUID): ResponseEntity<String> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = courseApiService.deleteCourse(courseId, userId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/bookmark")
    fun addBookmark(@RequestBody requestCourseIdDTO: RequestCourseIdDTO): ResponseEntity<String> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = courseApiService.addBookmark(requestCourseIdDTO.courseId, userId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/all")
    fun getAllCourses(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<PagedResponse<ResponseCourseDTO>> {
        val pageable = PageRequest.of(page, size)
        val userId = SecurityUtil.getCurrentUserId()
        val courses = courseApiService.getAllCourses(userId, pageable)

        val pagedResponse = PagedResponse(
            content = courses.content,
            totalPages = courses.totalPages,
            totalElements = courses.totalElements,
            currentPage = courses.number,
            pageSize = courses.size
        )

        return ResponseEntity.ok(pagedResponse)
    }

    @PatchMapping("/bookmark/remove")
    fun removeBookmark(@RequestBody requestCourseIdDTO: RequestCourseIdDTO): ResponseEntity<String> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = courseApiService.removeBookmark(requestCourseIdDTO.courseId, userId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/search/{title}")
    fun searchCoursesByTitle(
        @PathVariable title: String,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<PagedResponse<ResponseCourseDTO>> {
        val pageable = PageRequest.of(page, size)
        val userId = SecurityUtil.getCurrentUserId()
        val courses = courseApiService.searchCoursesByTitle(title, userId, pageable)

        val pagedResponse = PagedResponse(
            content = courses.content,
            totalPages = courses.totalPages,
            totalElements = courses.totalElements,
            currentPage = courses.number,
            pageSize = courses.size
        )

        return ResponseEntity.ok(pagedResponse)
    }

    @PostMapping("/hits")
    fun increaseHits(@RequestBody requestCourseIdDTO: RequestCourseIdDTO): ResponseEntity<String> {
        val result = courseApiService.increaseHits(requestCourseIdDTO.courseId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/recommend")
    fun getRecommendedCourses(): ResponseEntity<List<ResponseRecommendCourseDTO>> {
        val userId = SecurityUtil.getCurrentUserId()
        val recommendedCourses = courseApiService.getCombinedRecommendCourses(userId)
        return ResponseEntity.ok(recommendedCourses)
    }


    @PostMapping("/auto-generate")
    fun autoGenerateCourse(
        @RequestParam("question") question: String
    ): ResponseEntity<Map<String, Any>> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = courseApiService.createCourseByLLM(question, userId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/search/tag/{tagId}")
    fun searchCoursesByTag(
        @PathVariable tagId: UUID,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<PagedResponse<ResponseCourseDTO>> {
        val pageable = PageRequest.of(page, size)
        val userId = SecurityUtil.getCurrentUserId()
        val courses = courseApiService.searchCoursesByTag(tagId, userId, pageable)

        val pagedResponse = PagedResponse(
            content = courses.content,
            totalPages = courses.totalPages,
            totalElements = courses.totalElements,
            currentPage = courses.number,
            pageSize = courses.size
        )

        return ResponseEntity.ok(pagedResponse)
    }


}