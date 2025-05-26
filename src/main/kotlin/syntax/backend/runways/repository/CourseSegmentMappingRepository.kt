package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.CourseSegmentMapping

interface CourseSegmentMappingRepository : JpaRepository<CourseSegmentMapping, Long> {

}