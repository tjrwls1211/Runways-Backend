package syntax.backend.runways.service

import syntax.backend.runways.dto.FineDustDataDTO

interface FineDustService {
    fun getFineDustData(x:Double, y:Double): FineDustDataDTO
}