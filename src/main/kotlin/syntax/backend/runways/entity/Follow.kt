package syntax.backend.runways.entity

// USER 테이블 칼럼
data class Follow(
    val followings: MutableList<String> = mutableListOf(),
    val followers: MutableList<String> = mutableListOf()
) {
    fun addFollowing(userId: String) {
        if (!followings.contains(userId)) {
            followings.add(userId)
        }
    }
    fun removeFollowing(userId: String) {
        followings.remove(userId)
    }
    fun addFollower(userId: String) {
        if (!followers.contains(userId)) {
            followers.add(userId)
        }
    }
    fun removeFollower(userId: String) {
        followers.remove(userId)
    }
    fun isFollowing(userId: String): Boolean {
        return followings.contains(userId)
    }
}