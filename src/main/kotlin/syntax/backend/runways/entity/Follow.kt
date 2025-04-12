package syntax.backend.runways.entity

// USER 테이블 칼럼
data class Follow(
    val following: MutableList<String> = mutableListOf(),
    val followers: MutableList<String> = mutableListOf()
) {
    fun addFollowing(userId: String) {
        if (!following.contains(userId)) {
            following.add(userId)
        }
    }
    fun removeFollowing(userId: String) {
        following.remove(userId)
    }
    fun addFollower(userId: String) {
        if (!followers.contains(userId)) {
            followers.add(userId)
        }
    }
    fun removeFollower(userId: String) {
        followers.remove(userId)
    }
}