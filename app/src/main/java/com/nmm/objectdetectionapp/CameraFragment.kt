package com.nmm.objectdetectionapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.nmm.objectdetectionapp.databinding.FragmentCameraBinding
import com.nmm.objectdetectionapp.viewmodel.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.nmm.objectdetectionapp.analysis.LuminosityAnalyzer

/**
 * CameraFragment uses cameraX API to capture photos
 * It handles camera permissions, displays camera preview,
 * and captures images which are saved to external storage.
 */
class CameraFragment : Fragment() {

    // Binding object instance for accessing the fragment's views
    private var _binding: FragmentCameraBinding? = null

    //Null safety is handled by a getter
    private val binding get() = _binding!!

    // Navigation controller for navigating between fragments.
    private lateinit var navController: NavController

    // SharedViewModel for sharing data between fragments
    private lateinit var sharedViewModel: SharedViewModel

    // CameraX image capture use-case for taking photos.
    private var imageCapture: ImageCapture? = null
    // Executor service for running camera operations on a background thread.
    private lateinit var cameraExecutor: ExecutorService

    /**
     * Launcher for requesting camera and storage permissions.
     * Starts the camera if permissions are granted, otherwise displays a toast and closes the activity.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionGranted = permissions.entries.all { it.value }
            if (permissionGranted) {
                startCamera()
            } else {
                Toast.makeText(context,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        navController = findNavController()

        //Check permissions and start the camera or request permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            //A call to launch the permission request
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
        // Set up the listeners for taking photo
        binding.captureBtn.setOnClickListener {
            takePhoto()
//            navigateToImagePreview()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * Starts the camera and binds use cases.
     * Initialises and the camera provider and configures the Preview, ImageCapture, and ImageAnalysis use cases.
     * It selects the back camera as the default
     * @throws Exception if the use case binding fails, indicating an issue with camera initialisation.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Setup camera use cases
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()//New
            //Instantiating an instance of LuminosityAnalyzer in the ImageAnalysis
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }

            // Select back camera as a default camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll() // Unbind use cases before rebinding
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector,
                    preview, imageCapture, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Takes a photo with the ImageCapture use case and saves it
     * to the device's external storage in the "Pictures/Object Detection-Images" directory.
     * It supports JPEG images
     *
     * After capturing the photo, if successful, it corrects the image orientation (if needed),
     * updates the SharedViewModel with the URI of the captured image, and navigates to the ImagePreviewFragment.
     */
    private fun takePhoto() {
        // Ensure the Fragment is attached to an Activity
        val activity = activity ?: return

        // Ensure imageCapture is initialised
        val imageCapture = imageCapture ?: return

        // Generate a file name based on the  UK's current time
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.UK).format(System.currentTimeMillis())

        // Prepare content values for the saved image
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            // For Android Q and above, use relative path instead of absolute path
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Object Detection-Images")
            }
        }

        // Define output options for the captured image
        // using the Activity's contentResolver
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            activity.contentResolver, // Use the safely unwrapped Activity reference
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        // Execute capture and save image
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    // Corrects the image orientation and navigates to the preview
                    output.savedUri?.let { uri ->
                        correctImageOrientationAndSave(requireContext(), uri)

                        // Update SharedViewModel with the Uri of the captured image
                        sharedViewModel.setImageUri(uri)

                        // After updating the SharedViewModel, navigate to ImagePreviewFragment
                        navController.navigate(R.id.action_cameraFragment_to_imagePreviewFragment2)
                    }
                }

            }
        )
    }
    /**
     * Corrects the orientation of a captured image based on its EXIF orientation data.
     * After determining the necessary rotation, it applies this rotation to the bitmap representation
     * of the image and overwrites the original image file with the corrected bitmap.
     *
     * @param context The context used to access the ContentResolver for the image Uri.
     * @param imageUri The Uri of the captured image that may need orientation correction.
     */
    @SuppressLint("Recycle")
    private fun correctImageOrientationAndSave(context: Context, imageUri: Uri) {
        // Obtain an input stream from the given image Uri to read the image's EXIF data
        val inputStream = context.contentResolver.openInputStream(imageUri)

        // Use ExifInterface to parse the image's EXIF data from the input stream
        val exifInterface = ExifInterface(inputStream!!)

        // Determine the rotation degrees from the EXIF orientation tag
        // This indicates how much the image needs to be rotated to be displayed correctly
        val rotationDegrees = when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0 // No rotation needed
        }
        // Check if the image needs to be rotated
        if (rotationDegrees != 0) {

            @Suppress("DEPRECATION")
            // Convert the Uri to a Bitmap for manipulation
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)

            // Prepare a Matrix object to perform the rotation
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }

            // Apply the rotation to the Bitmap
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // obtain an output stream to write the rotated image back to its original Uri
            val outStream = context.contentResolver.openOutputStream(imageUri)

            // compress and write the rotated bitmap to the output stream, overwriting the original image
            outStream?.let { rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            outStream?.close() // Ensure to close the output stream after writing
        }
    }

    /**
     * Checks if all required permissions have been granted.
     * @return True if all permissions are granted, false otherwise.
     */
    private fun allPermissionsGranted() =
        // Iterate through the list of required permissions
        REQUIRED_PERMISSIONS.all {
            // for each permission in the list, check if it has been granted
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Cleans up resources and shuts down the camera executor when the view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown() //Shuts down the executor service
    }

    companion object {
        //Array of required permissions needed for the camera and external storage access
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
//                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        private const val TAG = "ObjectDetectionApp" // Tag for debugging

        //the format for naming  image files. It uses a timestamp to ensure that each filename is unique
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}

