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

    // 코스 생성
    @PostMapping("/insert")
    fun createCourse(@RequestBody requestCourseDTO: RequestCourseDTO): ResponseEntity<UUID> {
        val userId = SecurityUtil.getCurrentUserId()
        val newCourseId = courseApiService.createCourse(requestCourseDTO, userId)
        return ResponseEntity.ok(newCourseId)
    }

    // 코스 리스트 조회
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

    // 코스 수정
    @PatchMapping("/update")
    fun updateCourse(@RequestBody requestUpdateCourseDTO: RequestUpdateCourseDTO): ResponseEntity<UUID> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = courseApiService.updateCourse(requestUpdateCourseDTO, userId)
        return ResponseEntity.ok(result)
    }

    // 코스 상세 조회
    @GetMapping("/{id}")
    fun getCourseById(@PathVariable id: UUID): ResponseEntity<ResponseCourseDetailDTO> {
        val userId = SecurityUtil.getCurrentUserId()
        val course = courseApiService.getCourseById(id, userId)
        return ResponseEntity.ok(course)
    }

    // 코스 삭제
    @DeleteMapping("/delete/{courseId}")
    fun deleteCourse(@PathVariable courseId: UUID): ResponseEntity<String> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = courseApiService.deleteCourse(courseId, userId)
        return ResponseEntity.ok(result)
    }

    // 북마크 추가
    @PostMapping("/bookmark")
    fun addBookmark(@RequestBody requestCourseIdDTO: RequestCourseIdDTO): ResponseEntity<String> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = courseApiService.addBookmark(requestCourseIdDTO.courseId, userId)
        return ResponseEntity.ok(result)
    }

    // 코스 전체 조회
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

    // 북마크 삭제
    @PatchMapping("/bookmark/remove")
    fun removeBookmark(@RequestBody requestCourseIdDTO: RequestCourseIdDTO): ResponseEntity<String> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = courseApiService.removeBookmark(requestCourseIdDTO.courseId, userId)
        return ResponseEntity.ok(result)
    }

    // 코스 검색
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

    // 코스 조회수 증가
    @PostMapping("/hits")
    fun increaseHits(@RequestBody requestCourseIdDTO: RequestCourseIdDTO): ResponseEntity<String> {
        val result = courseApiService.increaseHits(requestCourseIdDTO.courseId)
        return ResponseEntity.ok(result)
    }

    // 추천 코스 조회
    @GetMapping("/recommend")
    fun getRecommendedCourses(): ResponseEntity<List<ResponseRecommendCourseDTO>> {
        val userId = SecurityUtil.getCurrentUserId()
        val recommendedCourses = courseApiService.getCombinedRecommendCourses(userId)
        return ResponseEntity.ok(recommendedCourses)
    }

    // 태그로 코스 검색
    @GetMapping("/search/tag/{tagName}")
    fun searchCoursesByTag(
        @PathVariable tagName: String,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<PagedResponse<ResponseCourseDTO>> {
        val pageable = PageRequest.of(page, size)
        val userId = SecurityUtil.getCurrentUserId()
        val courses = courseApiService.searchCoursesByTag(tagName, userId, pageable)

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