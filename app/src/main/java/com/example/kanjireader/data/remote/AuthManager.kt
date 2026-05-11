package com.example.kanjireader.data.remote

class AuthManager {
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    fun signIn(email: String, pass: String, onResult: (Boolean, Exception?) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            onResult(false, Exception("Puste pola"))
            return
        }
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task -> onResult(task.isSuccessful, task.exception) }
    }

    fun signUp(email: String, pass: String, onResult: (Boolean, Exception?) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            onResult(false, Exception("Brak dane"))
            return
        }
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task -> onResult(task.isSuccessful, task.exception) }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getUserId(): String? = auth.currentUser?.uid
}