package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import syntax.backend.runways.entity.TagLog
import syntax.backend.runways.repository.projection.TagScoreProjection
import java.util.UUID

interface TagLogRepository : JpaRepository<TagLog, UUID> {
    @Query(
        value = """
        SELECT 
            t.id AS id,
            t.name AS name,
            SUM(
                (
                    CASE
                        WHEN tl.action_type = 'USED' THEN 5
                        WHEN tl.action_type = 'CLICKED' THEN 3
                        WHEN tl.action_type = 'SEARCHED' THEN 2
                        ELSE 0
                    END
                ) * EXP(-0.1 * DATE_PART('day', now() - tl.created_at))
            ) AS score,
            MAX(tl.created_at) AS lastUsed
        FROM tag_logs tl
        JOIN tags t ON tl.tag_id = t.id
        WHERE tl.user_id = :userId
          AND tl.created_at > now() - interval '30 days'
        GROUP BY t.id, t.name
        ORDER BY score DESC, lastUsed DESC
        LIMIT 10
    """,
        nativeQuery = true
    )

    fun findWeightedTagsByUser(@Param("userId") userId: String): List<TagScoreProjection>
}