package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.FollowProfileDTO
import syntax.backend.runways.dto.RequestUserInfoDTO
import syntax.backend.runways.dto.ResponseMyInfoDTO
import syntax.backend.runways.dto.UserProfileWithCoursesDTO
import syntax.backend.runways.dto.UserRankingDTO
import syntax.backend.runways.entity.User

interface UserApiService {
    fun getUserDataFromToken(token: String): User
    fun getUserDataFromId(userId: String): User
    fun getUserInfoFromUserId(userId: String, pageable: Pageable): ResponseMyInfoDTO
    fun updateUserInfo(userId: String, requestUserInfoDTO: RequestUserInfoDTO) : Int
    fun isNicknameDuplicate(nickname: String): Boolean
    fun deleteUser(userId: String)
    fun registerDeviceId(userId: String, deviceId:String)
    fun addFollow(senderId: String, receiverId: String)
    fun removeFollowing(senderId: String, receiverId: String)
    fun removeFollower(senderId: String, receiverId: String)
    fun getFollowerList(userId: String): List<FollowProfileDTO>
    fun getFollowingList(userId: String): List<FollowProfileDTO>
    fun getUserInfoFromId(senderId: String,receiverId: String, pageable: Pageable): UserProfileWithCoursesDTO
    fun getRankingList(pageable: Pageable): Page<UserRankingDTO>
}