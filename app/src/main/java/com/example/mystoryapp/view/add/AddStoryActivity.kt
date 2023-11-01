package com.example.mystoryapp.view.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mystoryapp.R
import com.example.mystoryapp.ViewModelFactory
import com.example.mystoryapp.data.Result
import com.example.mystoryapp.data.getImageUri
import com.example.mystoryapp.data.reduceFileImage
import com.example.mystoryapp.data.uriToFile
import com.example.mystoryapp.databinding.ActivityAddStoryBinding
import com.example.mystoryapp.view.main.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddStoryActivity : AppCompatActivity() {
    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionGranted()) {
            requestPermission.launch(REQUIRED_PERMISSION)
        }

        val launcherIntentCamera = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ){isSuccess ->
            if (isSuccess){
                showImage()
            }
        }

        postStory()

        binding.cameraButton.setOnClickListener {
            currentImageUri = getImageUri(this)
            launcherIntentCamera.launch(currentImageUri)
        }

        binding.galleryButton.setOnClickListener {
            launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.uploadButton.setOnClickListener {
            var token: String
            currentImageUri?.let{ uri ->
                val imageFile = uriToFile(uri, this).reduceFileImage()
                val description = binding.edtDescription.text.toString()

                if (description.isNullOrEmpty()) {
                    AlertDialog.Builder(this).apply {
                        setTitle("Please tell us about your story :)")
                        setMessage(getString(R.string.empty_description))
                        setCancelable(false)
                        setPositiveButton(getString(R.string.ok_message)) { _, _ ->
                            val intent = Intent(context, AddStoryActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        create()
                        show()
                    }
                } else {
                    showLoading(true)

                    val requestBody = description.toRequestBody("text/plain".toMediaType())
                    val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                    val multipartBody = MultipartBody.Part.createFormData(
                        "photo",
                        imageFile.name,
                        requestImageFile
                    )

                    viewModel.getSession().observe(this) { user ->
                        token = user.token
                        viewModel.addStory(token, multipartBody, requestBody)
                    }
                }
            }?: showToast(getString(R.string.empty_image))
        }
    }

    private fun postStory() {
        viewModel.addStoryResponse.observe(this){
            when (it){
                is Result.Loading -> {
                    showLoading(true)
                    disableInterface()
                }
                is  Result.Success -> {
                    showLoading(false)
                    enableInterface()
                    AlertDialog.Builder(this).apply {
                        setTitle("Alright")
                        setMessage(getString(R.string.upload_message))
                        setCancelable(false)
                        setPositiveButton(getString(R.string.next_message)) { _, _ ->
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        create()
                        show()
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    enableInterface()
                }
            }
        }
    }

    private fun disableInterface() {
        binding.cameraButton.isEnabled = false
        binding.galleryButton.isEnabled = false
        binding.uploadButton.isEnabled = false
        binding.edtDescription.isEnabled = false
    }

    private fun enableInterface() {
        binding.cameraButton.isEnabled = true
        binding.galleryButton.isEnabled = true
        binding.uploadButton.isEnabled = true
        binding.edtDescription.isEnabled = true
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
        when (isGranted){
            true -> {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()}
            false -> {Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()}
        }
    }

    private fun allPermissionGranted() =
        ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED


    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "show Image:$it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if(isLoading) View.VISIBLE else View.GONE
    }
    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}