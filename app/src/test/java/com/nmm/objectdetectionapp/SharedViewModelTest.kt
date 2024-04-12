package com.nmm.objectdetectionapp



import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nmm.objectdetectionapp.viewmodel.SharedViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import android.net.Uri

@RunWith(MockitoJUnitRunner::class)
class SharedViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var uriObserver: Observer<Uri?>

    @Test
    fun `when setting ImageUri then observers are notified`() {
        val viewModel = SharedViewModel()
        viewModel.imageUri.observeForever(uriObserver)

        val testUri: Uri? = Uri.parse("android.resource://com.nmm.objectdetectionapp/drawable/test_image")
        viewModel.setImageUri(testUri)

        verify(uriObserver, times(1)).onChanged(testUri)
    }
}

