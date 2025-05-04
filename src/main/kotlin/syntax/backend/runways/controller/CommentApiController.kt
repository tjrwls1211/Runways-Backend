package syntax.backend.runways.controller

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.RequestInsertCommentDTO
import syntax.backend.runways.dto.ResponseCommentDTO
import syntax.backend.runways.dto.UpdateCommentDTO
import syntax.backend.runways.service.CommentApiService
import syntax.backend.runways.util.SecurityUtil
import java.util.UUID

@RestController
@RequestMapping("api/comment")
class CommentApiController(
    private val commentApiService: CommentApiService
) {
    // 댓글 조회
    @GetMapping("/list")
    fun getCommentList(
        @RequestParam courseId: UUID,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<Page<ResponseCommentDTO>> {
        val userId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size)
        val parentComments = commentApiService.getParentCommentList(courseId, pageable, userId)
        return ResponseEntity.ok(parentComments)
    }

    // 답글 조회
    @GetMapping("/list/{parentId}")
    fun getChildCommentList(
        @PathVariable parentId: UUID,
        @RequestParam courseId: UUID,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<Page<ResponseCommentDTO>> {
        val userId = SecurityUtil.getCurrentUserId()
        val pageable = PageRequest.of(page, size)
        val childComments = commentApiService.getChildCommentList(parentId, courseId, pageable, userId)
        return ResponseEntity.ok(childComments)
    }

    // 댓글 입력
    @PostMapping("/insert")
    fun insertComment(@RequestBody requestInsertCommentDTO: RequestInsertCommentDTO): ResponseEntity<ResponseCommentDTO> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = commentApiService.insertComment(requestInsertCommentDTO, userId)
        return ResponseEntity.ok(result)
    }

    // 댓글 업데이트
    @PatchMapping("/update")
    fun updateComment(@RequestBody updateCommentDTO: UpdateCommentDTO ): ResponseEntity<String> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = commentApiService.updateComment(updateCommentDTO, userId)
        return ResponseEntity.ok(result)
    }

    // 댓글 삭제
    @DeleteMapping("/delete/{commentId}")
    fun deleteComment(@PathVariable commentId: UUID): ResponseEntity<String> {
        val userId = SecurityUtil.getCurrentUserId()
        val result = commentApiService.deleteComment(commentId, userId)
        return ResponseEntity.ok(result)
    }
}