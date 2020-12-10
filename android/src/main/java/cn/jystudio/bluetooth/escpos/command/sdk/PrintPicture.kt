package cn.jystudio.bluetooth.escpos.command.sdk

import android.graphics.*
import cn.jystudio.bluetooth.escpos.command.sdk.PrintPicture.eachLinePixToCmd

import java.io.UnsupportedEncodingException
import kotlin.experimental.inv
import kotlin.experimental.or

object PrintPicture {
  private val p0 = intArrayOf(0, 128)
  private val p1 = intArrayOf(0, 64)
  private val p2 = intArrayOf(0, 32)
  private val p3 = intArrayOf(0, 16)
  private val p4 = intArrayOf(0, 8)
  private val p5 = intArrayOf(0, 4)
  private val p6 = intArrayOf(0, 2)
  private val Floyd16x16 = arrayOf(intArrayOf(0, 128, 32, 160, 8, 136, 40, 168, 2, 130, 34, 162, 10, 138, 42, 170), intArrayOf(192, 64, 224, 96, 200, 72, 232, 104, 194, 66, 226, 98, 202, 74, 234, 106), intArrayOf(48, 176, 16, 144, 56, 184, 24, 152, 50, 178, 18, 146, 58, 186, 26, 154), intArrayOf(240, 112, 208, 80, 248, 120, 216, 88, 242, 114, 210, 82, 250, 122, 218, 90), intArrayOf(12, 140, 44, 172, 4, 132, 36, 164, 14, 142, 46, 174, 6, 134, 38, 166), intArrayOf(204, 76, 236, 108, 196, 68, 228, 100, 206, 78, 238, 110, 198, 70, 230, 102), intArrayOf(60, 188, 28, 156, 52, 180, 20, 148, 62, 190, 30, 158, 54, 182, 22, 150), intArrayOf(252, 124, 220, 92, 244, 116, 212, 84, 254, 126, 222, 94, 246, 118, 214, 86), intArrayOf(3, 131, 35, 163, 11, 139, 43, 171, 1, 129, 33, 161, 9, 137, 41, 169), intArrayOf(195, 67, 227, 99, 203, 75, 235, 107, 193, 65, 225, 97, 201, 73, 233, 105), intArrayOf(51, 179, 19, 147, 59, 187, 27, 155, 49, 177, 17, 145, 57, 185, 25, 153), intArrayOf(243, 115, 211, 83, 251, 123, 219, 91, 241, 113, 209, 81, 249, 121, 217, 89), intArrayOf(15, 143, 47, 175, 7, 135, 39, 167, 13, 141, 45, 173, 5, 133, 37, 165), intArrayOf(207, 79, 239, 111, 199, 71, 231, 103, 205, 77, 237, 109, 197, 69, 229, 101), intArrayOf(63, 191, 31, 159, 55, 183, 23, 151, 61, 189, 29, 157, 53, 181, 21, 149), intArrayOf(254, 127, 223, 95, 247, 119, 215, 87, 253, 125, 221, 93, 245, 117, 213, 85))


  fun resizeImage(bitmap: Bitmap, w: Int, h: Int): Bitmap {
    val width = bitmap.getWidth()
    val height = bitmap.getHeight()
    val scaleWidth = w.toFloat() / width.toFloat()
    val scaleHeight = h.toFloat() / height.toFloat()
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
  }

  fun pad(Src: Bitmap, padding_x: Int, padding_y: Int): Bitmap {
    val outputimage = Bitmap.createBitmap(Src.getWidth() + padding_x, Src.getHeight() + padding_y, Bitmap.Config.ARGB_8888)
    val can = Canvas(outputimage)
    can.drawARGB(255, 255, 255, 255) //This represents White color
    can.drawBitmap(Src, padding_x.toFloat(), padding_y.toFloat(), null)
    return outputimage
  }


  /**
   * 打印位图函数
   * 此函数是将一行作为一个图片打印，这样处理不容易出错
   *
   * @param mBitmap
   * @param nWidth
   * @param nMode
   * @return
   */
  fun POS_PrintBMP(mBitmap: Bitmap, nWidth: Int, nMode: Int, leftPadding: Int): ByteArray {
    // 先转黑白，再调用函数缩放位图
    val width = (nWidth + 7) / 8 * 8
    var height = mBitmap.getHeight() * width / mBitmap.getWidth()
    height = (height + 7) / 8 * 8
    val left = if (leftPadding == 0) 0 else (leftPadding + 7) / 8 * 8

    var rszBitmap = mBitmap
    if (mBitmap.getWidth() !== width) {
      rszBitmap = Bitmap.createScaledBitmap(mBitmap, width, height, true)
    }

    var grayBitmap = toGrayscale(rszBitmap)
    if (left > 0) {
      grayBitmap = pad(grayBitmap, left, 0)
    }

    val dithered = thresholdToBWPic(grayBitmap)

    return eachLinePixToCmd(dithered, width + left, nMode)
  }

  /**
   * 使用下传位图打印图片
   * 先收完再打印
   *
   * @param bmp
   * @return
   */
  fun Print_1D2A(bmp: Bitmap): ByteArray {

    /*
			 * 使用下传位图打印图片
			 * 先收完再打印
			 */
    val width = bmp.getWidth()
    val height = bmp.getHeight()
    val data = ByteArray(1024 * 10)
    data[0] = 0x1D
    data[1] = 0x2A
    data[2] = ((width - 1) / 8 + 1).toByte()
    data[3] = ((height - 1) / 8 + 1).toByte()
    var k: Byte = 0
    var position = 4
    var i: Int
    var j: Int
    var temp: Byte = 0
    i = 0
    while (i < width) {
      j = 0
      while (j < height) {
        if (bmp.getPixel(i, j) !== -1) {
          temp = temp or (0x80 shr k.toInt()).toByte()
        } // end if
        k++
        if (k.toInt() == 8) {
          data[position++] = temp
          temp = 0
          k = 0
        } // end if k
        j++
      }// end for j
      if (k % 8 != 0) {
        data[position++] = temp
        temp = 0
        k = 0
      }
      i++

    }

    if (width % 8 != 0) {
      i = height / 8
      if (height % 8 != 0) i++
      j = 8 - width % 8
      k = 0
      while (k < i * j) {
        data[position++] = 0
        k++
      }
    }
    return data
  }

  fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
    val width: Int
    val height: Int
    height = bmpOriginal.getHeight()
    width = bmpOriginal.getWidth()

    val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    val c = Canvas(bmpGrayscale)
    val paint = Paint()
    val cm = ColorMatrix()
    cm.setSaturation(0.toFloat())
    val f = ColorMatrixColorFilter(cm)
    paint.setColorFilter(f)
    c.drawBitmap(bmpOriginal, 0.toFloat(), 0.toFloat(), paint)
    return bmpGrayscale
  }

  fun thresholdToBWPic(mBitmap: Bitmap): ByteArray {
    val pixels = IntArray(mBitmap.getWidth() * mBitmap.getHeight())
    val data = ByteArray(mBitmap.getWidth() * mBitmap.getHeight())
    mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0, mBitmap.getWidth(), mBitmap.getHeight())
    format_K_threshold(pixels, mBitmap.getWidth(), mBitmap.getHeight(), data)
    return data
  }

  private fun format_K_threshold(orgpixels: IntArray, xsize: Int, ysize: Int, despixels: ByteArray) {
    var graytotal = 0
    val grayave = true
    var k = 0

    var i: Int
    var j: Int
    var gray: Int
    i = 0
    while (i < ysize) {
      j = 0
      while (j < xsize) {
        gray = orgpixels[k] and 255
        graytotal += gray
        ++k
        ++j
      }
      ++i
    }

    val var10 = graytotal / ysize / xsize
    k = 0

    i = 0
    while (i < ysize) {
      j = 0
      while (j < xsize) {
        gray = orgpixels[k] and 255
        if (gray > var10) {
          despixels[k] = 0
        } else {
          despixels[k] = 1
        }

        ++k
        ++j
      }
      ++i
    }

  }

  fun eachLinePixToCmd(src: ByteArray, nWidth: Int, nMode: Int): ByteArray {
    val nHeight = src.size / nWidth
    val nBytesPerLine = nWidth / 8
    val data = ByteArray(nHeight * (8 + nBytesPerLine))
    val offset = false
    var k = 0

    for (i in 0 until nHeight) {
      val var10 = i * (8 + nBytesPerLine)
      //GS v 0 m xL xH yL yH d1....dk 打印光栅位图
      data[var10 + 0] = 29//GS
      data[var10 + 1] = 118//v
      data[var10 + 2] = 48//0
      data[var10 + 3] = (nMode and 1).toByte()
      data[var10 + 4] = (nBytesPerLine % 256).toByte()//xL
      data[var10 + 5] = (nBytesPerLine / 256).toByte()//xH
      data[var10 + 6] = 1//yL
      data[var10 + 7] = 0//yH

      for (j in 0 until nBytesPerLine) {
        data[var10 + 8 + j] = (p0[src[k].toInt()] + p1[src[k + 1].toInt()] + p2[src[k + 2].toInt()] + p3[src[k + 3].toInt()] + p4[src[k + 4].toInt()] + p5[src[k + 5].toInt()] + p6[src[k + 6].toInt()] + src[k + 7].toInt()).toByte()
        k += 8
      }
    }

    return data
  }

  fun pixToTscCmd(src: ByteArray): ByteArray {
    val data = ByteArray(src.size / 8)
    var k = 0

    var j = 0
    while (k < data.size) {
      val temp = (p0[src[j].toInt()] + p1[src[j + 1].toInt()] + p2[src[j + 2].toInt()] + p3[src[j + 3].toInt()] + p4[src[j + 4].toInt()] + p5[src[j + 5].toInt()] + p6[src[j + 6].toInt()] + src[j + 7].toInt()).toByte()
      data[k] = temp.inv().toByte()
      j += 8
      ++k
    }

    return data
  }

  fun pixToEscRastBitImageCmd(src: ByteArray): ByteArray {
    val data = ByteArray(src.size / 8)
    var i = 0

    var k = 0
    while (i < data.size) {
      data[i] = (p0[src[k].toInt()] + p1[src[k + 1].toInt()] + p2[src[k + 2].toInt()] + p3[src[k + 3].toInt()] + p4[src[k + 4].toInt()] + p5[src[k + 5].toInt()] + p6[src[k + 6].toInt()] + src[k + 7].toInt()).toByte()
      k += 8
      ++i
    }

    return data
  }

  fun pixToEscNvBitImageCmd(src: ByteArray, width: Int, height: Int): ByteArray {
    val data = ByteArray(src.size / 8 + 4)
    data[0] = (width / 8 % 256).toByte()
    data[1] = (width / 8 / 256).toByte()
    data[2] = (height / 8 % 256).toByte()
    data[3] = (height / 8 / 256).toByte()
    val k = false

    for (i in 0 until width) {
      var var7 = 0

      for (j in 0 until height / 8) {
        data[4 + j + i * height / 8] = (p0[src[i + var7].toInt()] + p1[src[i + var7 + 1 * width].toInt()] + p2[src[i + var7 + 2 * width].toInt()] + p3[src[i + var7 + 3 * width].toInt()] + p4[src[i + var7 + 4 * width].toInt()] + p5[src[i + var7 + 5 * width].toInt()] + p6[src[i + var7 + 6 * width].toInt()] + src[i + var7 + 7 * width].toInt()).toByte()
        var7 += 8 * width
      }
    }

    return data
  }

  fun bitmapToBWPix(mBitmap: Bitmap): ByteArray {
    val pixels = IntArray(mBitmap.getWidth() * mBitmap.getHeight())
    val data = ByteArray(mBitmap.getWidth() * mBitmap.getHeight())
    val grayBitmap = toGrayscale(mBitmap)
    grayBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0, mBitmap.getWidth(), mBitmap.getHeight())
    format_K_dither16x16(pixels, grayBitmap.getWidth(), grayBitmap.getHeight(), data)
    return data
  }

  private fun format_K_dither16x16(orgpixels: IntArray, xsize: Int, ysize: Int, despixels: ByteArray) {
    var k = 0

    for (y in 0 until ysize) {
      for (x in 0 until xsize) {
        if (orgpixels[k] and 255 > Floyd16x16[x and 15][y and 15]) {
          despixels[k] = 0
        } else {
          despixels[k] = 1
        }

        ++k
      }
    }

  }

}
