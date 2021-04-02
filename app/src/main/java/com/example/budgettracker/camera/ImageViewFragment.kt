package com.example.budgettracker.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.budgettracker.R
import com.example.budgettracker.databinding.FragmentImageviewBinding
import com.example.budgettracker.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_imageview.*
import kotlinx.coroutines.flow.collect
import java.io.File

private const val FILE_NAME = "image.jpg"
private const val REQUEST_CODE = 10
private lateinit var imageFile: File

@AndroidEntryPoint
class ImageViewFragment : Fragment(R.layout.fragment_imageview) {

    private val viewModel: CameraFragmentViewModel by viewModels()

    private lateinit var safeContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentImageviewBinding.bind(view)

        //
        if (ContextCompat.checkSelfPermission(
                safeContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                110
            )
            takePicture()
        } else {
            takePicture()
        }
        //Navigate to the detail receipt fragment
        binding.apply {
            goToReceipt.setOnClickListener {
                viewModel.onGoToDetailViewClick()
            }
        }
        //define scope so as to be cancelled when onStop is called and restarted when
        //onStart is called
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.cameraEvent.collect { event ->
                when (event) {
                    //Navigate to the receipt fragment
                    is CameraFragmentViewModel.CameraEvent.NavigateToReceiptDetailScreen -> {
                        val action =
                            ImageViewFragmentDirections.actionImageViewFragmentToReceiptFragment(
                                event.receipt
                            )
                        findNavController().navigate(action)
                    }
                    is CameraFragmentViewModel.CameraEvent.ImageProcessingCompleted -> {
                        //Change the displayed button when image processing is complete
                        binding.loadingButton.visibility = View.GONE
                        binding.goToReceipt.visibility = View.VISIBLE
                    }
                    is CameraFragmentViewModel.CameraEvent.NoSupportedReceiptDetected -> {
                        //Change the displayed button if no supported receipt is detected
                        binding.loadingButton.visibility = View.GONE
                        binding.noReceiptDetected.visibility = View.VISIBLE
                    }
                    is CameraFragmentViewModel.CameraEvent.IncompleteDataReceived -> {
                        //Change the displayed button if incomplete data is received
                        binding.loadingButton.visibility = View.GONE
                        binding.incompleteData.visibility = View.VISIBLE
                    }
                    is CameraFragmentViewModel.CameraEvent.StoreNotSupported -> {
                        //Change the displayed button if a particular store is not supported
                        binding.loadingButton.visibility = View.GONE
                        binding.storeNotSupported.visibility = View.VISIBLE
                    }
                }.exhaustive
            }
        }
    }


    //Alternative to onActivityResult
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //
            val takenImage = BitmapFactory.decodeFile(imageFile.absolutePath)
            imageView.setImageBitmap(takenImage)
            //pass the captured image to textRecognition in viewModel
            viewModel.textRecognition(takenImage)
        }
    }

    //
    private fun takePicture() {

        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageFile = getImageFile(FILE_NAME)
        //check for a camera
        val fileProvider =
            FileProvider.getUriForFile(safeContext, "com.example.fileprovider", imageFile)
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        resultLauncher.launch(takePicture)
    }

    private fun getImageFile(fileName: String): File {
        //access package-specific directories
        val storageDirectory = safeContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }


}