package cn.jystudio.bluetooth.tsc

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import cn.jystudio.bluetooth.BluetoothService
import cn.jystudio.bluetooth.BluetoothServiceStateObserver
import com.facebook.react.bridge.*
import java.util.Vector

/**
 * Created by januslo on 2018/9/22.
 */
class RNBluetoothTscPrinterModule(reactContext: ReactApplicationContext, private val mService: BluetoothService) : ReactContextBaseJavaModule(reactContext), BluetoothServiceStateObserver {

  override fun getName(): String {
    return "BluetoothTscPrinter"
  }

  init {
    this.mService.addStateObserver(this)
  }

  @ReactMethod
  fun printLabel(options: ReadableMap, promise: Promise) {
    val width = options.getInt("width")
    val height = options.getInt("height")
    val gap = if (options.hasKey("gap")) options.getInt("gap") else 0
    val speed = if (options.hasKey("speed")) this.findSpeed(options.getInt("speed")) else null
    val enable = if (options.hasKey("tear"))
      if (options.getString("tear").equals(TscCommand.ENABLE.ON.value, ignoreCase = true)) TscCommand.ENABLE.ON else TscCommand.ENABLE.OFF
    else
      TscCommand.ENABLE.OFF
    val texts = if (options.hasKey("text")) options.getArray("text") else null
    val qrCodes = if (options.hasKey("qrcode")) options.getArray("qrcode") else null
    val barCodes = if (options.hasKey("barcode")) options.getArray("barcode") else null
    val images = if (options.hasKey("image")) options.getArray("image") else null
    val reverses = if (options.hasKey("reverse")) options.getArray("reverse") else null

    val direction = if (options.hasKey("direction"))
      if (TscCommand.DIRECTION.BACKWARD.value === options.getInt("direction")) TscCommand.DIRECTION.BACKWARD else TscCommand.DIRECTION.FORWARD
    else
      TscCommand.DIRECTION.FORWARD
    //        Not Support Yet
    //        TscCommand.MIRROR mirror = options.hasKey("mirror") ?
    //                TscCommand.MIRROR.MIRROR.getValue() == options.getInt("mirror") ? TscCommand.MIRROR.MIRROR : TscCommand.MIRROR.NORMAL
    //                : TscCommand.MIRROR.NORMAL;
    val density = if (options.hasKey("density")) this.findDensity(options.getInt("density")) else null
    val reference = if (options.hasKey("reference")) options.getArray("reference") else null

    var sound = false
    if (options.hasKey("sound") && options.getInt("sound") === 1) {
      sound = true
    }
    var home = false
    if (options.hasKey("home") && options.getInt("home") === 1) {
      home = true
    }
    val tsc = TscCommand()
    if (speed != null) {
      tsc.addSpeed(speed)//设置打印速度
    }
    if (density != null) {
      tsc.addDensity(density)//设置打印浓度
    }
    tsc.addSize(width, height) //设置标签尺寸，按照实际尺寸设置
    tsc.addGap(gap)           //设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
    tsc.addDirection(direction)//设置打印方向
    //设置原点坐标
    if (reference != null && reference!!.size() === 2) {
      tsc.addReference(reference!!.getInt(0), reference!!.getInt(1))
    } else {
      tsc.addReference(0, 0)
    }
    tsc.addTear(enable) //撕纸模式开启
    if (home) {
      tsc.addBackFeed(16)
      tsc.addHome()//走纸到开始位置
    }
    tsc.addCls()// 清除打印缓冲区
    //绘制简体中文
    run {
      var i = 0
      while (texts != null && i < texts!!.size()) {
        val text = texts!!.getMap(i)
        var t = text!!.getString("text")
        val x = text.getInt("x")
        val y = text.getInt("y")
        val fonttype = this.findFontType(text.getString("fonttype")!!)
        val rotation = this.findRotation(text.getInt("rotation"))
        val xscal = this.findFontMul(text.getInt("xscal"))
        val yscal = this.findFontMul(text.getInt("xscal"))
        val bold = text.hasKey("bold") && text.getBoolean("bold")

        try {
          val temp = t!!.toByteArray(charset("UTF-8"))
          val temStr = String(temp, charset("UTF-8"))
          t = String(temStr.toByteArray(charset("GB2312")), charset("GB2312"))//打印的文字
        } catch (e: Exception) {
          promise.reject("INVALID_TEXT", e)
          return
        }

        tsc.addText(x, y, fonttype/*字体类型*/,
          rotation/*旋转角度*/, xscal/*横向放大*/, yscal/*纵向放大*/, t)

        if (bold) {
          tsc.addText(x + 1, y, fonttype,
            rotation, xscal, yscal, t/*这里的t可能需要替换成同等长度的空格*/)
          tsc.addText(x, y + 1, fonttype,
            rotation, xscal, yscal, t/*这里的t可能需要替换成同等长度的空格*/)
        }
        i++
      }
    }

    //绘制图片
    if (images != null) {
      for (i in 0 until images!!.size()) {
        val img = images!!.getMap(i)
        val x = img!!.getInt("x")
        val y = img.getInt("y")
        val imgWidth = img.getInt("width")
        val mode = this.findBitmapMode(img.getInt("mode"))
        val image = img.getString("image")
        val decoded = Base64.decode(image, Base64.DEFAULT)
        val b = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
        tsc.addBitmap(x, y, mode, imgWidth, b)
      }
    }

    if (qrCodes != null) {
      for (i in 0 until qrCodes!!.size()) {
        val qr = qrCodes!!.getMap(i)
        val x = qr!!.getInt("x")
        val y = qr.getInt("y")
        val qrWidth = qr.getInt("width")
        val level = this.findEEC(qr.getString("level")!!)
        val rotation = this.findRotation(qr.getInt("rotation"))
        val code = qr.getString("code")
        tsc.addQRCode(x, y, level, qrWidth, rotation, code!!)
      }
    }
    if (barCodes != null) {
      for (i in 0 until barCodes!!.size()) {
        val bar = barCodes!!.getMap(i)
        val x = if (bar!!.hasKey("x")) bar.getInt("x") else 0
        val y = if (bar.hasKey("y")) bar.getInt("y") else 0
        val barHeight = if (bar.hasKey("height")) bar.getInt("height") else 200
        val barWide = if (bar.hasKey("wide")) bar.getInt("wide") else 2
        val narrow = if (bar.hasKey("narrow")) bar.getInt("narrow") else 1
        val rotation = this.findRotation(bar.getInt("rotation"))
        val code = bar.getString("code")
        val type = this.findBarcodeType(bar.getString("type")!!)
        val readable = this.findReadable(bar.getInt("readable"))
        tsc.add1DBarcode(x, y, type, barHeight, barWide, narrow, readable, rotation, code!!)
      }
    }

    if (reverses != null) {
      for (i in 0 until reverses!!.size()) {
        val area = reverses!!.getMap(i)
        val ax = area!!.getInt("x")
        val ay = area.getInt("y")
        val aWidth = area.getInt("width")
        val aHeight = area.getInt("height")
        tsc.addReverse(ax, ay, aWidth, aHeight)
      }
    }

    tsc.addPrint(1, 1) // 打印标签
    if (sound) {
      tsc.addSound(2, 100) //打印标签后 蜂鸣器响
    }
    val bytes = tsc.command
    val tosend = ByteArray(bytes!!.size)
    for (i in bytes.indices) {
      tosend[i] = bytes.get(i)
    }
    if (sendDataByte(tosend)) {
      promise.resolve(null)
    } else {
      promise.reject("COMMAND_SEND_ERROR")
    }
  }

  private fun findBarcodeType(type: String): TscCommand.BARCODETYPE {
    var barcodeType = TscCommand.BARCODETYPE.CODE128
    for (t in TscCommand.BARCODETYPE.values()) {
      if (("" + t.value).equals(type, ignoreCase = true)) {
        barcodeType = t
        break
      }
    }
    return barcodeType
  }

  private fun findReadable(readable: Int): TscCommand.READABLE {
    var ea = TscCommand.READABLE.EANBLE
    if (TscCommand.READABLE.DISABLE.value === readable) {
      ea = TscCommand.READABLE.DISABLE
    }
    return ea
  }

  private fun findFontMul(scan: Int): TscCommand.FONTMUL {
    var mul = TscCommand.FONTMUL.MUL_1
    for (m in TscCommand.FONTMUL.values()) {
      if (m.value === scan) {
        mul = m
        break
      }
    }
    return mul
  }

  private fun findRotation(rotation: Int): TscCommand.ROTATION {
    var rt = TscCommand.ROTATION.ROTATION_0
    for (r in TscCommand.ROTATION.values()) {
      if (r.value === rotation) {
        rt = r
        break
      }
    }
    return rt
  }

  private fun findFontType(fonttype: String): TscCommand.FONTTYPE {
    var ft = TscCommand.FONTTYPE.FONT_CHINESE
    for (f in TscCommand.FONTTYPE.values()) {
      if (("" + f.value).equals(fonttype, ignoreCase = true)) {
        ft = f
        break
      }
    }
    return ft
  }


  private fun findSpeed(speed: Int): TscCommand.SPEED? {
    var sd: TscCommand.SPEED? = null
    when (speed) {
      1 -> sd = TscCommand.SPEED.SPEED1DIV5
      2 -> sd = TscCommand.SPEED.SPEED2
      3 -> sd = TscCommand.SPEED.SPEED3
      4 -> sd = TscCommand.SPEED.SPEED4
    }
    return sd
  }

  private fun findEEC(level: String): TscCommand.EEC {
    var eec = TscCommand.EEC.LEVEL_L
    for (e in TscCommand.EEC.values()) {
      if (e.value.equals(level, ignoreCase = true)) {
        eec = e
        break
      }
    }
    return eec
  }

  private fun findDensity(density: Int): TscCommand.DENSITY? {
    var ds: TscCommand.DENSITY? = null
    for (d in TscCommand.DENSITY.values()) {
      if (d.value === density) {
        ds = d
        break
      }
    }
    return ds
  }

  private fun findBitmapMode(mode: Int): TscCommand.BITMAP_MODE {
    var bm = TscCommand.BITMAP_MODE.OVERWRITE
    for (m in TscCommand.BITMAP_MODE.values()) {
      if (m.value === mode) {
        bm = m
        break
      }
    }
    return bm
  }

  private fun sendDataByte(data: ByteArray): Boolean {
    if (mService.state !== BluetoothService.STATE_CONNECTED) {
      return false
    }
    mService.write(data)
    return true
  }

  override fun onBluetoothServiceStateChanged(state: Int, boundle: Map<String, Any>?) {

  }

  companion object {
    private val TAG = "BluetoothTscPrinter"
  }
}
