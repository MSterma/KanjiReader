package com.example.kanjireader.data.remote
class AuthManager {
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    fun signIn(email: String, pass: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { onResult(it.isSuccessful) }
    }
    fun signUp(email: String, pass: String, onResult: (Boolean) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            onResult(false)
            return
        }
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }
    // data/remote/AuthManager.kt
    fun signOut() {
        auth.signOut()
    }
    fun getUserId(): String? = auth.currentUser?.uid
}