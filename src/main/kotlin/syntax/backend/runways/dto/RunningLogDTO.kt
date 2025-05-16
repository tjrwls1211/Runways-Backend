package syntax.backend.runways.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import syntax.backend.runways.entity.RunningLog
import java.time.LocalDateTime
import java.util.UUID

data class RunningLogDTO(
    val id: UUID,
    val userId: String,
    val courseId: String?,
    val distance: Float,
    val duration: Long,
    val avgSpeed: Float,
    val maxSpeed: Float,
    val position: ObjectNode?,
    val coordinate: ObjectNode?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
) {
    companion object {
        private val objectMapper = ObjectMapper()

        fun from(runningLog: RunningLog): RunningLogDTO {
            val geoJsonWriter = org.locationtech.jts.io.geojson.GeoJsonWriter()

            val positionGeoJson = geoJsonWriter.write(runningLog.position)
            val coordinateGeoJson = geoJsonWriter.write(runningLog.coordinate)

            val positionNode = objectMapper.readTree(positionGeoJson) as ObjectNode
            val coordinateNode = objectMapper.readTree(coordinateGeoJson) as ObjectNode

            return RunningLogDTO(
                id = runningLog.id,
                userId = runningLog.user.id,
                courseId = runningLog.course?.id?.toString(),
                distance = runningLog.distance,
                duration = runningLog.duration,
                avgSpeed = runningLog.avgSpeed,
                maxSpeed = runningLog.maxSpeed,
                position = positionNode,
                coordinate = coordinateNode,
                startTime = runningLog.startTime,
                endTime = runningLog.endTime
            )
        }
    }
}