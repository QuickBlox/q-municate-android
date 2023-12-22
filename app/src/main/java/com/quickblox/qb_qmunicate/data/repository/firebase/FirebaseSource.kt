package com.quickblox.qb_qmunicate.data.repository.firebase

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.quickblox.qb_qmunicate.BuildConfig.FIREBASE_APP_ID
import java.util.concurrent.TimeUnit

class FirebaseSource {
    fun getToken(): String {
        val firebaseUser = getUser()
        if (isUserNotCorrect(firebaseUser)) {
            throw IllegalArgumentException("Firebase User is not logged in")
        }

        val task = firebaseUser?.getIdToken(false)
        if (task == null) {
            throw IllegalArgumentException("Firebase Task is null")
        }

        val token = Tasks.await(task, 10, TimeUnit.SECONDS).token

        if (isTokenNotCorrect(token)) {
            throw IllegalArgumentException("Firebase Access token is not exist")
        }

        return token!!
    }

    private fun getUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    private fun isUserNotCorrect(firebaseUser: FirebaseUser?): Boolean {
        return firebaseUser == null || firebaseUser.isAnonymous
    }

    private fun isTokenNotCorrect(accessToken: String?): Boolean {
        return accessToken.isNullOrEmpty()
    }

    fun getProjectId(): String {
        val projectId = FIREBASE_APP_ID
        if (isProjectIdNotCorrect(projectId)) {
            throw IllegalArgumentException("Firebase Project Id is not exist")
        }
        return projectId!!
    }

    private fun isProjectIdNotCorrect(projectId: String?): Boolean {
        return projectId.isNullOrEmpty()
    }
}