package com.seanghay.numberscanner

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.seanghay.numberscanner.databinding.FragmentScannerBinding
import logcat.logcat

class ScannerFragment : Fragment() {

  private var _binding: FragmentScannerBinding? = null
  private val binding: FragmentScannerBinding get() = _binding!!

  private val surfaceCallback = object : SurfaceHolder.Callback {
    override fun surfaceCreated(holder: SurfaceHolder) {
      prepareCamera()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
      logcat { "surfaceChanged" }
      drawBoundBox(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }
  }

  private fun drawBoundBox(holder: SurfaceHolder) {

    val width = binding.previewView.width
    val height = binding.previewView.height

    val boxSize = height / 2.4f

    val left = (width - boxSize) / 2f
    val right = left + boxSize
    val top = (height - boxSize) / 2f
    val bottom = top + boxSize

    val canvas = holder.lockCanvas()
    canvas.drawColor(0, PorterDuff.Mode.CLEAR)

    val paint = Paint()

    paint.style = Paint.Style.FILL
    paint.color = Color.parseColor("#90000000")
    canvas.drawRect(left, top, right, bottom, paint)

    paint.style = Paint.Style.STROKE
    paint.color = Color.WHITE
    paint.strokeWidth = 5f

    canvas.drawRect(left, top, right, bottom, paint)

    holder.unlockCanvasAndPost(canvas)
  }

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
    prepareOverlay()
  }

  override fun onDestroyView() {
    _binding = null
    super.onDestroyView()
  }

  private fun prepareOverlay() {
    binding.surfaceViewOverlay.apply {
      setZOrderOnTop(true)
      holder.also {
        it.setFormat(PixelFormat.TRANSPARENT)
        it.addCallback(surfaceCallback)
      }
    }
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