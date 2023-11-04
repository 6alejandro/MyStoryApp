package com.example.mystoryapp.view.add

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mystoryapp.R
import com.example.mystoryapp.ViewModelFactory
import com.example.mystoryapp.data.Result
import com.example.mystoryapp.data.getImageUri
import com.example.mystoryapp.data.reduceFileImage
import com.example.mystoryapp.data.uriToFile
import com.example.mystoryapp.databinding.ActivityAddStoryBinding
import com.example.mystoryapp.view.main.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AddStoryActivity : AppCompatActivity() {
    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null

    private var currentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permission ->
            when {
                permission[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                permission[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                else -> binding.switchLocation.isChecked = false
            }
        }

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
                        viewModel.addStory(token, multipartBody, requestBody, currentLocation)
                    }
                }
            }?: showToast(getString(R.string.empty_image))
        }
        binding.switchLocation.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                if (!isGpsEnabled()) {
                    showGpsMessage()
                }
                lifecycleScope.launch {
                    getMyLastLocation()
                }
            } else {
                currentLocation = null
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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

    private fun checkpermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    @SuppressLint("MissingPermission")
    private fun getMyLastLocation() {
        if (checkpermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            (checkpermission(Manifest.permission.ACCESS_COARSE_LOCATION))
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLocation = location
                    } else {
                        Toast.makeText(
                            this,
                            R.string.error_location,
                            Toast.LENGTH_SHORT
                        ).show()
                        getNewLocation()
                    }
                }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getNewLocation() {
        Toast.makeText(this.baseContext, "Get new location", Toast.LENGTH_SHORT).show()
        val locationRequest = com.google.android.gms.location.LocationRequest()
        locationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = TimeUnit.SECONDS.toMillis(1)
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
            Looper.myLooper()?.let {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, it
                )
            }
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            currentLocation = p0.lastLocation
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showGpsMessage() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.gps_message_title))
            setMessage(getString(R.string.gps_message))
            setPositiveButton(getString(R.string.next_message)) { _,_ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
            create()
            show()
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