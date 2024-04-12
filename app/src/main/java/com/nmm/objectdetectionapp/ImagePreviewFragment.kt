package com.nmm.objectdetectionapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
//import android.media.ExifInterface
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.nmm.objectdetectionapp.databinding.FragmentImagePreviewBinding
import com.nmm.objectdetectionapp.model.MyModel
import com.nmm.objectdetectionapp.viewmodel.SharedViewModel
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * Fragment for previewing an image selected by the user and classifying it using a TensorFlow Lite model.
 *It handles permissions for reading external storage, picking an image from the device,
 * and processing the image for classification.
 */
@Suppress("DEPRECATION")
class ImagePreviewFragment : Fragment() {
    // Fragment initialisation parameter param1
    private var param1: String? = null

    // Binding object for interacting with the fragment's views
    private var _binding: FragmentImagePreviewBinding? = null
    //Null safety is handled by a getter
    private val binding get() = _binding!!

    // NavController for managing app navigation
    private lateinit var navController: NavController
    // SharedViewModel for sharing data between fragments
    private lateinit var sharedViewModel: SharedViewModel

    // URI of the selected image
    private var imageUri: String? = null

    // Launchers for handling the result of image picking and permission requests
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // TensorFlow Lite interpreter for running image classification
    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise the imagePickerLauncher for handling image selection result
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imagePreview.setImageURI(it)
                classifyImage(it) // Ensure this is called with the selected image URI
            }
        }

        // Initialise the requestPermissionLauncher for the permission request result
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openImagePicker()
            } else {
                Toast.makeText(context, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
            }
        }
        // Retrieving fragment arguments
        arguments?.let { it ->
            param1 = it.getString(ARG_PARAM1)
            imageUri = it.getString("imageUri")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Store a reference to the NavController for navigating between fragments
        navController = findNavController()
        // Initialise the SharedViewModel to share data between fragments
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        // Load the TensorFlow Lite model for image classification.
        loadModel()

        // Observe changes to the imageUri LiveData in the SharedViewModel.
        // When a new URI is set, display the image and classify it.
        sharedViewModel.imageUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                // Set the ImageView to display the image located at the URI
                binding.imagePreview.setImageURI(it)

                // Call classifyImage with the URI to classify the image
                // It handles exceptions internally to prevent app crashes
                classifyImage(it)
            }
        }
        // Setup a click listener for the 'Upload' button which allows the user to pick an image
        binding.uploadBtn.setOnClickListener {
            openImagePicker()
        }
        // Setup a click listener for navigating to the CameraFragment
        navigateToCamera()
    }

    /**
     * Requests permission to read external storage if not already granted,
     * or directly opens the image picker.
     */
    private fun openImagePicker() {
        // Check if the app has the READ_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
            ) {
            // Permission is granted, launch the image picker
            imagePickerLauncher.launch("image/*")
        } else {
            // Permission is not granted, request the permission
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Loads the TensorFlow Lite model from the assets folder.
     * The model is loaded into memory using memory mapping, and an instance of the Interpreter
     * class is created with the loaded model.
     */
    private fun loadModel() {
        // Open an AssetFileDescriptor for the model file in the assets folder
        val assetFileDescriptor = requireContext().assets.openFd("model.tflite")

        // Create a FileInputStream from the AssetFileDescriptor
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)

        // Get a FileChannel from the FileInputStream
        val fileChannel = fileInputStream.channel

        // Get the start offset and declared length of the asset file
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength

        // Map the FileChannel into a read-only memory-mapped buffer
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        // Create an instance of the Interpreter class with the loaded model buffer
        tflite = Interpreter(modelBuffer)
    }

    /**
     * Performs image classification on the selected image.
     * @param imageUri URI of the image to classify.
     */
    private fun classifyImage(imageUri: Uri) {

        try {
            // Convert imageUri to Bitmap and preprocess it
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
//            Log.d("ImagePreviewFragment", "Bitmap created successfully.")

            // Get the correct rotation for the image
            val degree = getRotationDegree(imageUri, requireContext())

            val processedBitmap = if (degree != 0) {
                Log.d("ImagePreviewFragment", "Applying rotation: $degree")
                rotateBitmap(bitmap, degree)
            } else {
//                Log.d("ImagePreviewFragment", "No rotation applied")
                bitmap
            }.let {
                Bitmap.createScaledBitmap(it, 224, 224, true)
            }

            val modelInput = convertBitmapToByteBuffer(processedBitmap)

            // Prepare the model's input and output objects
            val output = Array(1) { FloatArray(NUM_CLASSES) } // Using the NUM_CLASSES constant
            tflite.run(modelInput, output)

            // Extract the results and display them
            val results = output[0]
            displayResults(results)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to process image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
//            Log.e("ImagePreviewFragment", "Error in classifyImage", e)
        }
    }

    /**
     * This function takes a URI pointing to an image file and the current Context object,
     * and returns the rotation degree of the image based on its EXIF orientation data.
     *
     * @param imageUri The URI of the image file whose rotation degree needs to be determined.
     * @param context The current Context object.
     * @return The rotation degree of the image in degrees (0, 90, 180, or 270).
     */
    private fun getRotationDegree(imageUri: Uri, context: Context): Int {
        // Open an input stream from the image URI using the ContentResolver.
        val inputStream = context.contentResolver.openInputStream(imageUri)

        // Create an ExifInterface object from the input stream
        val exifInterface = inputStream?.let { ExifInterface(it) }

        // Get the orientation value from the EXIF data
        val orientation = exifInterface?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )
//        Log.d("ImagePreviewFragment", "EXIF orientation: $orientation")

        // Return the rotation degree based on the orientation value
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90 // Image is rotated 90 degrees clockwise
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0 // No rotation needed
        }
    }

    /**
     * Rotates the original bitmap based on the given degree.
     * @param originalBitmap The bitmap to rotate.
     * @param degree The degree to rotate the bitmap.
     * @return A new rotated bitmap.
     */
    private fun rotateBitmap(originalBitmap: Bitmap, degree: Int): Bitmap {
        // Create a new Matrix object for transformation
        val matrix = Matrix()

        // Apply rotation transformation to the matrix
        matrix.postRotate(degree.toFloat())

        // Create a new Bitmap object by applying the rotation transformation
        return Bitmap.createBitmap(
            originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
        )
    }

    /**
     * Displays classification results on the UI.
     * @param results The classification results to display.
     */
    @SuppressLint("StringFormatInvalid")
    private fun displayResults(results: FloatArray) {
        //The labels
        val labels = listOf("Keyboard", "Mouse", "Pen", "Battery",
            "Headphone", "Clock", "Book", "Mug", "Remote Control",
            "Pencil", "Laptop", "Sticky Note"
        )

        // Create a list of MyModel instances from the results and labels
        val modelResults = results.mapIndexed { index, confidence ->
            MyModel(labels[index], confidence)
        }

        // Sort the list by confidence in descending order
        val sortedResults = modelResults.sortedByDescending { it.confidence }

        // Update the UI on the main thread
        activity?.runOnUiThread {
            // Displaying the highest confidence and label
            val topResult = sortedResults.firstOrNull()
            binding.result.text = topResult?.let {
                getString(R.string.result_text, it.label)
            } ?: "No result"

            // Display top 3 confidences
            val top3ConfidencesText = sortedResults
                .take(3) // Take the top 3
                .joinToString("\n") {
                    "${it.label}: ${String.format("%.1f%%", it.confidence * 100)}"
                }
            binding.confidences.text = top3ConfidencesText
        }
    }

    /**
     * Converts a bitmap to a ByteBuffer to feed into the TensorFlow Lite model.
     * @param bitmap The bitmap to convert.
     * @return The converted ByteBuffer.
     */
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val `val` = intValues[pixel++] // Use the pixel value for model input
                byteBuffer.putFloat((`val` shr 16 and 0xFF) / 255f)
                byteBuffer.putFloat((`val` shr 8 and 0xFF) / 255f)
                byteBuffer.putFloat((`val` and 0xFF) / 255f)
            }
        }
        return byteBuffer
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            IMAGE_PICK_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission
                    openImagePicker()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    Toast.makeText(
                        context,
                        "Permission denied to read your External storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    /**
     * Handle the result from launching an external activity, specifically for picking an image from the device's storage.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     * allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if the request code matches the image pick request code and the result is OK
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Get the image URI from the intent data
            val imageUri: Uri? = data?.data

            // If the image URI is not null, update the ImageView and classify the image
            imageUri?.let { uri ->
                binding.imagePreview.setImageURI(uri)
                classifyImage(imageUri)
            }
        }
    }
    /**
     * Sets up the click listener for the 'Camera' button to navigate back to the CameraFragment.
     */
    private fun navigateToCamera() {
        binding.cameraBtn2.setOnClickListener {
            navController.navigate(R.id.action_imagePreviewFragment2_to_cameraFragment)
        }
    }
    
    companion object {
        /**
         * Use this factory method to create a new instance of ImagePreviewFragment.
         *
         * @param param1 A string parameter representing the image URI.
         * @return A new instance of fragment ImagePreviewFragment with the provided image URI.
         */
//
//        @JvmStatic
//        fun newInstance(param1: String) =
//            ImagePreviewFragment().apply {
//                arguments = Bundle().apply {
//                    putString("imageUri", param1)
//                }
//            }
        // A constant representing the request code for the image pick intent
        private const val IMAGE_PICK_REQUEST_CODE = 1001
        // A constant representing the number of classes for image classification
        private const val NUM_CLASSES = 12
    }
}