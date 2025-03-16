package syntax.backend.runways.entity

import jakarta.persistence.Entity
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "Locations")
data class Location(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", updatable = false, nullable = false)
    val id: UUID,

    @Column(name = "SIDO", nullable = false, length = 20)
    val sido: String,

    @Column(name = "SIGUNGU_FULL", nullable = false, length = 20)
    val sigunguFull: String,

    @Column(name = "SIGUNGU", nullable = false, length = 20)
    val sigungu: String,

    @Column(name = "X", nullable = false)
    val x: Int,

    @Column(name = "Y", nullable = false)
    val y: Int,

    @Column(name = "DAEGIOYEM", nullable = false, length = 20)
    val daegioyem: String
)