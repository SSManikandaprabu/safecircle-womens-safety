package com.safecircle.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.safecircle.app.data.local.dao.UserDao
import com.safecircle.app.data.local.entity.UserEntity
import com.safecircle.app.util.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) {

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isSignedIn: Boolean get() = auth.currentUser != null

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> =
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { cacheUser(it) }
            Result.Success(result.user!!)
        } catch (e: Exception) {
            Result.Error(e.toAuthMessage(), e)
        }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.let { user ->
            val profile = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profile).await()
            persistUserProfile(user, displayName)
            cacheUser(user)
        }
        Result.Success(result.user!!)
    } catch (e: Exception) {
        Result.Error(e.toAuthMessage(), e)
    }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        result.user?.let { user ->
            persistUserProfile(user, user.displayName ?: "SafeCircle User")
            cacheUser(user)
        }
        Result.Success(result.user!!)
    } catch (e: Exception) {
        Result.Error(e.toAuthMessage(), e)
    }

    fun signOut() {
        auth.signOut()
    }

    private suspend fun persistUserProfile(user: FirebaseUser, displayName: String) {
        val doc = firestore.collection("users").document(user.uid)
        val data = mapOf(
            "uid" to user.uid,
            "displayName" to displayName,
            "email" to (user.email ?: ""),
            "photoUrl" to user.photoUrl?.toString(),
            "createdAt" to System.currentTimeMillis()
        )
        doc.set(data).await()
    }

    private suspend fun cacheUser(user: FirebaseUser) {
        userDao.upsert(
            UserEntity(
                uid = user.uid,
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString(),
                cachedAt = System.currentTimeMillis()
            )
        )
    }

    private fun Exception.toAuthMessage(): String = when (this) {
        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
            "Invalid email or password."
        is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
            "No account found for this email."
        is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
            "An account already exists with this email."
        is com.google.firebase.auth.FirebaseAuthWeakPasswordException ->
            "Password must be at least 6 characters."
        else -> message ?: "Authentication failed. Please try again."
    }
}
