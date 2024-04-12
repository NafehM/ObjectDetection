package com.nmm.objectdetectionapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.nmm.objectdetectionapp.databinding.FragmentInstructionBinding
import com.nmm.objectdetectionapp.viewmodel.SharedViewModel

/**
 * InstructionFragment displays instructions to the user and offers navigation back to the HomeFragment.
 * It uses data binding for UI management and a SharedViewModel for data sharing across fragments.
 */
class InstructionFragment : Fragment() {

    // Binding object instance for accessing the fragment's views
    private var _binding: FragmentInstructionBinding? = null

    //Null safety is handled by a getter
    private val binding get() = _binding!!

    // NavController for managing app navigation
    private lateinit var navController: NavController

    // SharedViewModel for sharing data between fragments
    private lateinit var sharedViewModel: SharedViewModel

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
        _binding = FragmentInstructionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialise the SharedViewModel to share data between fragments
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        // Initialise NavController
        navController = findNavController()

        // Setup back button to navigate back to the HomeFragment
        navigateToHome() //navigate back to home
    }

    /**
     * Sets up the click listener for the back button, navigating back to the HomeFragment.
     */
    private fun navigateToHome() {
        binding.backBtn.setOnClickListener {
            navController.navigate(R.id.action_instructionFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clears the binding object to avoid memory leaks
        _binding = null
    }
}