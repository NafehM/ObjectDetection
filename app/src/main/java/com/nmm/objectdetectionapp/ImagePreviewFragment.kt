package com.nmm.objectdetectionapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.nmm.objectdetectionapp.databinding.FragmentImagePreviewBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ImagePreviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ImagePreviewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentImagePreviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    private var imageUri: String? = null

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imagePreview.setImageURI(it)
            }
        }

        // Initialize the ActivityResultLauncher for the permission request
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openImagePicker()
            } else {
                Toast.makeText(context, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
            }
        }

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            arguments?.let {
                imageUri = it.getString("imageUri")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        // Retrieve the imageUri argument
        val imageUri = arguments?.getString("imageUri")

        // Check if imageUri is null before parsing
        // Then parse the imageUri and set it to the ImageView using the binding object
        imageUri?.let {
            val uri = Uri.parse(it)
            binding.imagePreview.setImageURI(uri)
        }

        // Set up the click listener for the upload button
        binding.uploadBtn.setOnClickListener {
            openImagePicker()
        }

        //calling navigateToCamera() function to navigate back to the CameraFragment
        navigateToCamera()
    }

    private fun openImagePicker() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED) {
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/*"
//            startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
            imagePickerLauncher.launch("image/*")
        } else {
            // Request permission
//            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                IMAGE_PICK_REQUEST_CODE)
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data

            // Update the ImageView with the new image
            imageUri?.let { uri ->
                binding.imagePreview.setImageURI(uri)
            }
        }
    }


    /**
     * Sets up the click listener for the back button .
     * When the back button is clicked, the function uses the NavController to navigate
     * back to the CameraFragment.
     */
    private fun navigateToCamera() {
        binding.backButton.setOnClickListener {
            navController.navigate(R.id.action_imagePreviewFragment2_to_cameraFragment)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ImagePreviewFragment.
         */
        private const val IMAGE_PICK_REQUEST_CODE = 1001
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImagePreviewFragment().apply {
                arguments = Bundle().apply {
                    putString("imageUri", param1)
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}