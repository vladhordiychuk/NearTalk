package com.neartalk.data.repository

import com.neartalk.data.local.UserDao
import com.neartalk.data.local.entity.UserEntity
import com.neartalk.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getUserById(userId: Int): Flow<User?> {
        return userDao.getUserById(userId).map { entity ->
            entity?.toDomain()
        }
    }

    override fun getAllUsers(): Flow<List<User>> {
        println("DEBUG: Repository - Fetching all users")
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

private fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = name,
        username = username,
        phone = phone,
        email = email,
        birthday = birthday,
        status = status
    )
}
