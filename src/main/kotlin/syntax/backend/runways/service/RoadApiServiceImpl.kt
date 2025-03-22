package syntax.backend.runways.service

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.PrecisionModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.CoordinateDTO
import syntax.backend.runways.dto.RoadDataDTO
import syntax.backend.runways.repository.RoadApiRepository

@Service
class RoadApiServiceImpl @Autowired constructor(
    private val roadApiRepository: RoadApiRepository
) : RoadApiService {

    override fun getRoadDataById(id: Long): RoadDataDTO {
        val positionGeoJson = roadApiRepository.getGeoJsonByPosition(id)
        val routeGeoJson = roadApiRepository.getGeoJsonByRoute(id)

        return RoadDataDTO(
            position = positionGeoJson,
            route = routeGeoJson
        )
    }

    private fun getLineString(coordinateDTO: List<CoordinateDTO>): LineString {
        val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
        val coordinates = coordinateDTO.map { Coordinate(it.latitude, it.longitude) }.toTypedArray()
        return geometryFactory.createLineString(coordinates)
    }


}
