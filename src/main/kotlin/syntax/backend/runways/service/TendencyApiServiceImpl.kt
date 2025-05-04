package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.dto.TendencyDTO
import syntax.backend.runways.entity.Tendency
import syntax.backend.runways.repository.TendencyRepository

@Service
class TendencyApiServiceImpl (
    private val tendencyRepository: TendencyRepository,
    private val userApiService: UserApiService
): TendencyApiService {

    // 성향 저장
    override fun saveTendency(userId: String, tendencyDTO: TendencyDTO) {
        val user = userApiService.getUserDataFromId(userId)

        // 이미 성향 데이터가 존재하는 경우
        val existingTendency = tendencyRepository.findByUser(user)
        if (existingTendency != null) {
            // 성향 데이터 업데이트
            existingTendency.exerciseFrequency = tendencyDTO.exerciseFrequency
            existingTendency.runningLocation = tendencyDTO.runningLocation
            existingTendency.runningGoal = tendencyDTO.runningGoal
            existingTendency.exerciseDuration = tendencyDTO.exerciseDuration
            existingTendency.sleepDuration = tendencyDTO.sleepDuration
            tendencyRepository.save(existingTendency)
        } else {
            // 성향 데이터가 존재하지 않는 경우 새로 생성
            val newTendency = Tendency (
                user = user,
                exerciseFrequency = tendencyDTO.exerciseFrequency,
                runningLocation = tendencyDTO.runningLocation,
                runningGoal = tendencyDTO.runningGoal,
                exerciseDuration = tendencyDTO.exerciseDuration,
                sleepDuration = tendencyDTO.sleepDuration
            )
            tendencyRepository.save(newTendency)
        }
    }

    // 성향 데이터 확인
    override fun getTendency(usrId: String): TendencyDTO? {
        val user = userApiService.getUserDataFromId(usrId)
        val tendency = tendencyRepository.findByUser(user)

        return if (tendency != null) {
            TendencyDTO(
                exerciseFrequency = tendency.exerciseFrequency,
                runningLocation = tendency.runningLocation,
                runningGoal = tendency.runningGoal,
                exerciseDuration = tendency.exerciseDuration,
                sleepDuration = tendency.sleepDuration
            )
        } else {
            null
        }
    }
}