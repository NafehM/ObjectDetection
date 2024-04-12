package com.nmm.objectdetectionapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.nmm.objectdetectionapp.databinding.FragmentHomeBinding
import com.nmm.objectdetectionapp.viewmodel.SharedViewModel

//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * HomeFragment is the starting UI fragment of the app that allows users
 * to navigate to Camera or ImagePreview or Instruction.
 * It uses data binding for managing UI components
 * and employs a SharedViewModel for fragment data sharing.
 */
class HomeFragment : Fragment() {
//    private var param1: String? = null
//    private var param2: String? = null

    // Binding object instance for accessing the fragment's views
    private var _binding: FragmentHomeBinding? = null

    //Null safety is handled by a getter
    private val binding get() = _binding!!

    // NavController for managing app navigation
    private lateinit var navController: NavController

    // SharedViewModel for sharing data between fragments
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using data binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialise the SharedViewModel to share data between fragments
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        //Store a reference to the NavController for navigating between fragments
        navController = findNavController()

        // Setup navigation buttons
        navigateToCamera()
        navigateToImagePreview()
        navigateToInstruction()
    }

    /**
     * Sets up the click listener for the camera button to navigate to the CameraFragment.
     */
    private fun navigateToCamera() {
        binding.cameraBtn.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_cameraFragment)
        }
    }

    /**
     * Sets up the click listener for the image preview button to navigate to the ImagePreviewFragment.
     */
    private fun navigateToImagePreview() {
        binding.imagePreviewBtn.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_imagePreviewFragment2)
        }
    }

    /**
     * Sets up the click listener for the instruction button to navigate to InstructionFragment.
     */
    private fun navigateToInstruction() {
        binding.instructionBtn.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_instructionFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clears the binding object to avoid memory leaks
        _binding = null
    }
}
//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * HomeFragment using the provided parameters.
//         * This allows for passing data into the fragment at the time of its creation.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment HomeFragment.
//         */
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            HomeFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
