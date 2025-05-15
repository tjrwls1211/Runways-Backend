package syntax.backend.runways.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import syntax.backend.runways.dto.LlmRequestDTO
import syntax.backend.runways.dto.StatusMessageDTO
import syntax.backend.runways.service.CourseApiService
import java.util.UUID

@Controller
class StatusStreamController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val courseApiService: CourseApiService,
) {

    @MessageMapping("/status/course/generate")
    fun generateCourse(llmRequestDTO: LlmRequestDTO) {
        val session = "/topic/status/${llmRequestDTO.statusSessionId}"

        messagingTemplate.convertAndSend(session, StatusMessageDTO("RECEIVED", "요청을 접수했습니다", null))

        try {
            // 1. 프롬프트 생성
            messagingTemplate.convertAndSend(session, StatusMessageDTO("PROMPTING", "프롬프트 생성 중...", null))

            // 2. Ollama 요청
            messagingTemplate.convertAndSend(session, StatusMessageDTO("GENERATING", "Ollama에 요청 중...", null))
            val testId = "3947510376"
            val response = courseApiService.createCourseByLLM(llmRequestDTO, testId)

            messagingTemplate.convertAndSend(session, StatusMessageDTO("LLM_RESPONSE", "AI 응답 도착!", response))

            // 4. 완료
            messagingTemplate.convertAndSend(session, StatusMessageDTO("COMPLETED", "코스 생성 완료!", response))

        } catch (e: Exception) {
            messagingTemplate.convertAndSend(session, StatusMessageDTO("ERROR", "오류 발생: ${e.message}", null))
        }
    }
}