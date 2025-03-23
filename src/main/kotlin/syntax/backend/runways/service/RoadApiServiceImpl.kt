package syntax.backend.runways.service

import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.geojson.GeoJsonReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.RoadDataDTO
import syntax.backend.runways.entity.Road
import syntax.backend.runways.repository.RoadApiRepository

@Service
class RoadApiServiceImpl @Autowired constructor(
    private val roadApiRepository: RoadApiRepository
) : RoadApiService {

    override fun getRoadDataById(id: Long): RoadDataDTO {
        val optRoad = roadApiRepository.findById(id)
        if (optRoad.isEmpty) {
            throw IllegalArgumentException("Road not found")
        }

        val road = optRoad.get()
        val route = road.route
        val position = road.position

        val geoJsonWriter = GeoJsonWriter()

        val geoJsonPoint = geoJsonWriter.write(position)
        val geoJsonRoute = geoJsonWriter.write(route)

        return RoadDataDTO(
            position = geoJsonPoint,
            route = geoJsonRoute
        )
    }

    override fun saveRoadData(roadDataDTO: RoadDataDTO) {
        val geoJsonReader = GeoJsonReader()
        val position = geoJsonReader.read(roadDataDTO.position) as Point
        val route = geoJsonReader.read(roadDataDTO.route) as LineString

        val road = Road(
            position = position,
            route = route
        )

        roadApiRepository.save(road)
    }
}
