package com.seanghay.numberscanner

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.internal.ImageConvertUtils
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import logcat.logcat
import kotlin.math.roundToInt

class TextAnalyzer : ImageAnalysis.Analyzer {

  private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

  @SuppressLint("UnsafeOptInUsageError")
  override fun analyze(imageProxy: ImageProxy) {
    val image = imageProxy.image
    if (image == null) {
      imageProxy.close()
      return
    }

    if (image.format != ImageFormat.YUV_420_888) {
      return imageProxy.close()
    }

    val buffer = image.planes[0].buffer
    val bitmap = ImageConvertUtils.yv12ToBitmap(buffer, image.width, image.height, 0)


    val width = bitmap.width
    val height = bitmap.height

    val boxSize = height / 2f

    val left = (width - boxSize) / 2f
    val top = (height - boxSize) / 2f

    val bmp = Bitmap.createBitmap(bitmap, left.roundToInt(), top.roundToInt(), boxSize.roundToInt(), boxSize.roundToInt())
    val inputImage = InputImage.fromBitmap(
      bmp,
      imageProxy.imageInfo.rotationDegrees,
    )

    recognizer.process(
      inputImage
    ).addOnCompleteListener {
      if (!bmp.isRecycled) {
        bmp.recycle()
      }
      imageProxy.close()
    }.addOnSuccessListener { text ->
      logcat { "text=${text.text}" }
    }

  }


  private fun croppedNV21(mediaImage: Image, cropRect: Rect): ByteArray {
    val yBuffer = mediaImage.planes[0].buffer // Y
    val vuBuffer = mediaImage.planes[2].buffer // VU

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    return cropByteArray(nv21, mediaImage.width, cropRect)
  }

  private fun cropByteArray(array: ByteArray, imageWidth: Int, cropRect: Rect): ByteArray {
    val croppedArray = ByteArray(cropRect.width() * cropRect.height())
    var i = 0
    array.forEachIndexed { index, byte ->
      val x = index % imageWidth
      val y = index / imageWidth

      if (cropRect.left <= x && x < cropRect.right && cropRect.top <= y && y < cropRect.bottom) {
        croppedArray[i] = byte
        i++
      }
    }

    return croppedArray
  }
}