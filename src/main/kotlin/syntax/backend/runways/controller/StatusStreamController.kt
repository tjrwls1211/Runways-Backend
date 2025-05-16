package syntax.backend.runways.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import syntax.backend.runways.dto.LlmRequestDTO
import syntax.backend.runways.dto.StatusMessageDTO
import syntax.backend.runways.service.CourseApiService


@Controller
class StatusStreamController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val courseApiService: CourseApiService,
) {

    @MessageMapping("/status/course/generate")
    fun generateCourse(
        llmRequestDTO: LlmRequestDTO
    ) {
        val session = "/topic/status/${llmRequestDTO.statusSessionId}"
        println("generateCourse 호출됨, sessionId: ${llmRequestDTO.statusSessionId}")

        val auth = SecurityContextHolder.getContext().authentication
        println("SecurityContextHolder authentication: $auth")

        if (auth !is UsernamePasswordAuthenticationToken) {
            println("인증 정보 없음 또는 타입 불일치")
            messagingTemplate.convertAndSend(
                session,
                StatusMessageDTO("ERROR", "인증되지 않은 사용자입니다.", null)
            )
            return
        }

        if (auth.authorities.none { it.authority == "ROLE_USER" || it.authority == "ROLE_ADMIN" }) {
            println("권한 부족: ROLE_USER 필요")
            messagingTemplate.convertAndSend(
                session,
                StatusMessageDTO("ERROR", "USER 권한이 필요합니다.", null)
            )
            return
        }

        val userId = auth.name
        println("사용자 인증 완료: $userId")

        messagingTemplate.convertAndSend(session, StatusMessageDTO("RECEIVED", "요청을 접수했습니다", null))

        try {
            println("프롬프트 생성 단계 진입")
            messagingTemplate.convertAndSend(session, StatusMessageDTO("PROMPTING", "프롬프트 생성 중...", null))

            println("Ollama 요청 단계 진입")
            messagingTemplate.convertAndSend(session, StatusMessageDTO("GENERATING", "AI 서버에 요청 중...", null))

            val response = courseApiService.createCourseByLLM(llmRequestDTO, userId)

            println("코스 생성 완료")
            messagingTemplate.convertAndSend(session, StatusMessageDTO("COMPLETED", "코스 생성 완료!", response))

        } catch (e: Exception) {
            e.printStackTrace()
            messagingTemplate.convertAndSend(
                session,
                StatusMessageDTO("ERROR", "오류 발생: ${e.message}", null)
            )
        }
    }

}