package com.seanghay.numberscanner

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.seanghay.numberscanner.databinding.FragmentScannerBinding
import logcat.logcat
import java.util.concurrent.Executors

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

    val boxSize = height / 2.3f

    val left = (width - boxSize) / 2f
    val right = left + boxSize
    val top = (height - boxSize) / 2f
    val bottom = top + boxSize

    val canvas = holder.lockCanvas()

    val paint = Paint()
    paint.style = Paint.Style.FILL
    paint.color = Color.parseColor("#90000000")

    val path = Path()

    path.apply {
      moveTo(0f, 0f)
      lineTo(width.toFloat(), 0f)
      lineTo(width.toFloat(), height.toFloat())
      lineTo(0f, height.toFloat())
      lineTo(0f, 0f)
      close()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      canvas.clipOutRect(left, top, right, bottom)
    } else TODO("FAILED!!")

    canvas.drawPath(path, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = dpToPx(4f)
    paint.color = Color.parseColor("#aaffffff")

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
    val textExecutor = Executors.newSingleThreadExecutor()

    val runnable = Runnable {
      val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
      val cameraProvider = cameraProviderFuture.get()
      val preview = Preview.Builder().build()
      preview.setSurfaceProvider(binding.previewView.surfaceProvider)

      val analysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetResolution(Size(720, 1488))
        .build()


      analysis.setAnalyzer(textExecutor, TextAnalyzer())

      cameraProvider.unbindAll()
      cameraProvider.bindToLifecycle(
        viewLifecycleOwner,
        cameraSelector,
        analysis,
        preview
      )
    }

    cameraProviderFuture.addListener(runnable, executor)
  }


  private fun dpToPx(value: Float): Float {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      value,
      requireContext().resources.displayMetrics
    )
  }

}