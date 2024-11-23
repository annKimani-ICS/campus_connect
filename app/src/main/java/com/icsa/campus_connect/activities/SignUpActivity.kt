package com.icsa.campus_connect.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.icsa.campus_connect.R
import com.icsa.campus_connect.repository.User
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.MessageDigest

class SignUpActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var selectPhotoButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var userTypeRadioGroup: RadioGroup
    private var selectedPhoto: ByteArray? = null // Store the photo as a byte array

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference.child("users")
    private val storage = FirebaseStorage.getInstance().reference.child("profilePhotos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize views
        firstNameEditText = findViewById(R.id.firstNameEditText)
        middleNameEditText = findViewById(R.id.middleNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        selectPhotoButton = findViewById(R.id.selectPhotoButton)
        profileImageView = findViewById(R.id.profileImageView)
        userTypeRadioGroup = findViewById(R.id.userTypeRadioGroup)

        auth = FirebaseAuth.getInstance()

        // Select Photo Button Logic
        selectPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_SELECT_PHOTO)
        }

        // Sign-Up Button Logic
        signUpButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val middleName = middleNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // Get selected user type
            val userType = when (userTypeRadioGroup.checkedRadioButtonId) {
                R.id.studentRadioButton -> "Student"
                R.id.organiserRadioButton -> "Organiser"
                else -> ""
            }

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || userType.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All required fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register user
            registerUser(firstName, middleName, lastName, email, phone, userType, password)
        }
    }

    // Hashing function for passwords
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // Register user using Firebase Authentication
    private fun registerUser(
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        phone: String,
        userType: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val hashedPassword = hashPassword(password)
                    saveUserToDatabase(userId, firstName, middleName, lastName, email, phone, userType, hashedPassword)
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Save user information to Firebase Realtime Database
    private fun saveUserToDatabase(
        userId: String,
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        phone: String,
        userType: String,
        password: String
    ) {
        val user = User(userId, firstName, middleName, lastName, email, phone, userType, password)

        database.child(userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // If the profile photo is selected, upload it to Firebase Storage
                if (selectedPhoto != null) {
                    uploadProfilePhoto(userId)
                } else {
                    Toast.makeText(this, "Sign-Up Successful!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Failed to save user data. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Upload profile photo to Firebase Storage
    private fun uploadProfilePhoto(userId: String) {
        val photoRef = storage.child(userId)
        selectedPhoto?.let {
            photoRef.putBytes(it)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Get the download URL and update the user's profile
                        photoRef.downloadUrl.addOnSuccessListener { uri ->
                            database.child(userId).child("profilePhoto").setValue(uri.toString())
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        Toast.makeText(this, "Sign-Up Successful!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Failed to save profile photo URL. Try again.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(this, "Failed to upload profile photo. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle photo selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                profileImageView.setImageBitmap(bitmap) // Display the selected image

                // Convert Bitmap to ByteArray for storing in Firebase Storage
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                selectedPhoto = outputStream.toByteArray()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_PHOTO = 1
    }
}
