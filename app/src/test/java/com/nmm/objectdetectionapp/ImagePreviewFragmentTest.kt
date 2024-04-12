package com.nmm.objectdetectionapp

import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.nmm.objectdetectionapp.model.MyModel
import com.nmm.objectdetectionapp.viewmodel.SharedViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer

@RunWith(MockitoJUnitRunner::class)
class ImagePreviewFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var fragment: ImagePreviewFragment
    private lateinit var context: Context
    private val imageUri = Uri.parse("android.resource://com.nmm.objectdetectionapp/drawable/test_image")

    @Mock
    private lateinit var mockInterpreter: Interpreter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fragment = ImagePreviewFragment()

        // Mock TensorFlow Lite Interpreter
        val output = Array(1) { FloatArray(1) }
        output[0][0] = 0.9f // Mock confidence for the classification
//        `when`(mockInterpreter.run(anyOrNull(), anyOrNull())).thenAnswer {val outputArg = it.getArgument<Array<FloatArray>>(1)
//            outputArg[0] = output[0]}


//
//        `when`(mockInterpreter.run(any(ByteBuffer::class.java), any(Array<FloatArray>::class.java))).thenAnswer {
//            val outputArg = it.getArgument<Array<FloatArray>>(1)
//            outputArg[0] = output[0]
//        }

//        // Replace the real interpreter with a mock
//        fragment.tflite = mockInterpreter
//
//        // Assume SharedViewModel and LiveData setup is done here
//        fragment.sharedViewModel = SharedViewModel().apply {
//            imageUri = MutableLiveData(imageUri)
//        }
    }

    @Test
    fun classifyImage_WithMockInterpreter_UpdatesLiveData() {
        // Trigger classifyImage to use the mocked interpreter and LiveData
//        fragment.classifyImage(imageUri)

        // Assert that LiveData was updated correctly
        // You would observe LiveData and assert the expected value
        // This part of the code is illustrative; specific implementation may vary
        // based on how you manage LiveData and results in your fragment
    }

    // Helper methods to mock Uri to Bitmap conversion, etc., could be added here

}
