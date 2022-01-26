package com.seanghay.numberscanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.seanghay.numberscanner.databinding.FragmentScannerBinding

class ScannerFragment : Fragment() {

  private var _binding: FragmentScannerBinding? = null
  private val binding: FragmentScannerBinding get() = _binding!!


  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentScannerBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    prepareCamera()
  }

  override fun onDestroyView() {
    _binding = null
    super.onDestroyView()
  }

  private fun prepareCamera() {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    val executor = ContextCompat.getMainExecutor(requireContext())

    val runnable = Runnable {
      val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
      val cameraProvider = cameraProviderFuture.get()
      val preview = Preview.Builder().build()

      preview.setSurfaceProvider(binding.previewView.surfaceProvider)
      cameraProvider.unbindAll()
      cameraProvider.bindToLifecycle(
        viewLifecycleOwner,
        cameraSelector,
        preview
      )
    }

    cameraProviderFuture.addListener(runnable, executor)
  }

}