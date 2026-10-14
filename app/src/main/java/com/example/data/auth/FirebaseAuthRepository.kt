package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.User
import com.example.data.model.UserApprovalStatus
import com.example.data.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val context: Context,
    private val auth: FirebaseAuth = Firebase.auth
) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isRegisteringNewRole = MutableStateFlow(false)
    val isRegisteringNewRole: StateFlow<Boolean> = _isRegisteringNewRole.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val credentialManager = CredentialManager.create(context)

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _firebaseUser.value = user
            if (user != null) {
                loadUserProfile(user.uid)
            } else {
                _userProfile.value = null
                _isRegisteringNewRole.value = false
            }
        }
    }

    fun loadUserProfile(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val roleStr = doc.getString("role") ?: "STAFF"
                    val statusStr = doc.getString("approvalStatus") ?: "PENDING"
                    val fullName = doc.getString("fullName") ?: "Pengguna"
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phoneNumber") ?: ""

                    val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STAFF }
                    val status = try { UserApprovalStatus.valueOf(statusStr) } catch (e: Exception) { UserApprovalStatus.PENDING }

                    _userProfile.value = User(
                        id = uid,
                        username = email.substringBefore("@"),
                        fullName = fullName,
                        role = role,
                        status = status,
                        avatarEmoji = when (role) {
                            UserRole.ADMIN -> "👑"
                            UserRole.STAFF -> "👨‍🏫"
                            UserRole.KEAMANAN -> "👮‍♂️"
                        },
                        phone = phone
                    )
                    _isRegisteringNewRole.value = false
                } else {
                    // New user -> prompt role registration
                    _isRegisteringNewRole.value = true
                }
            }
            .addOnFailureListener {
                Log.e("FirebaseAuth", "Failed to load user profile: ${it.message}")
            }
    }

    fun registerUserProfile(
        role: UserRole,
        fullName: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onError("User belum login Google")
            return
        }

        val status = if (role == UserRole.ADMIN) {
            UserApprovalStatus.APPROVED
        } else {
            UserApprovalStatus.PENDING
        }

        val data = mapOf(
            "userId" to user.uid,
            "email" to (user.email ?: ""),
            "fullName" to fullName.ifBlank { user.displayName ?: "Pengguna" },
            "role" to role.name,
            "approvalStatus" to status.name,
            "phoneNumber" to phone.ifBlank { "0812345678" },
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(user.uid).set(data, SetOptions.merge())
            .addOnSuccessListener {
                _userProfile.value = User(
                    id = user.uid,
                    username = (user.email ?: "").substringBefore("@"),
                    fullName = fullName.ifBlank { user.displayName ?: "Pengguna" },
                    role = role,
                    status = status,
                    avatarEmoji = when (role) {
                        UserRole.ADMIN -> "👑"
                        UserRole.STAFF -> "👨‍🏫"
                        UserRole.KEAMANAN -> "👮‍♂️"
                    },
                    phone = phone
                )
                _isRegisteringNewRole.value = false
                onSuccess()
            }
            .addOnFailureListener {
                onError("Gagal mendaftarkan profil: ${it.message}")
            }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Konfigurasi Google Sign-In tidak ditemukan (default_web_client_id)")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    onSuccess()
                } else {
                    onError("Tipe kredensial tidak valid")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In dibatalkan: ${e.message}")
                onCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In gagal", e)
                onError(e.localizedMessage ?: "Gagal masuk dengan Google")
            }
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed to clear credential state", e)
            } finally {
                _userProfile.value = null
                _isRegisteringNewRole.value = false
                onComplete()
            }
        }
    }

    companion object {
        @Volatile
        private var instance: FirebaseAuthRepository? = null

        fun getInstance(context: Context): FirebaseAuthRepository {
            return instance ?: synchronized(this) {
                instance ?: FirebaseAuthRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
