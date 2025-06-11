package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.Road

@Repository
interface RoadApiRepository : CrudRepository<Road, Long> {

    @Query("SELECT ST_AsGeoJSON(r.position) FROM Road r WHERE r.id = :id")
    fun getGeoJsonByPosition(@Param("id") id: Long): String

    @Query("SELECT ST_AsGeoJSON(r.route) FROM Road r WHERE r.id = :id")
    fun getGeoJsonByRoute(@Param("id") id: Long): String
}
