package syntax.backend.runways.entity

import jakarta.persistence.*
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point

@Entity
@Table(name = "roads")
class Road {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   val id: Long? = null

    @Column(columnDefinition = "geometry(Point, 4326)")
    val position: Point? = null

    @Column(columnDefinition = "geometry(LineString, 4326)")
    val route: LineString? = null
}