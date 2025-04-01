package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.dto.RequestInsertCommentDTO
import syntax.backend.runways.entity.Deatgul
import syntax.backend.runways.repository.DeatgulApiRepository
import syntax.backend.runways.entity.CommentStatus

@Service
class DeatgulApiService(
    private val userApiService: UserApiService,
    private val deatgulApiRepository: DeatgulApiRepository,
) {

    fun insertDeatgul (requestInsertCommentDTO: RequestInsertCommentDTO, token: String): String {
        val user = userApiService.getUserDataFromToken(token)

        val newDeatGul = Deatgul (
            content = requestInsertCommentDTO.content,
            author = user,
            postId = requestInsertCommentDTO.courseId.toString(),
            status = CommentStatus.PUBLIC,
            parent = requestInsertCommentDTO.parentId.toString()
        )

        deatgulApiRepository.save(newDeatGul)

        return "댓글 작성 성공"
    }
}