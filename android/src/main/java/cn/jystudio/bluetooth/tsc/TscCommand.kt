package cn.jystudio.bluetooth.tsc

import android.graphics.Bitmap
import android.util.Log
import cn.jystudio.bluetooth.escpos.command.sdk.PrintPicture

import java.io.UnsupportedEncodingException
import java.util.Vector

class TscCommand {
  var command: Vector<Byte>? = null
    private set

  enum class FOOT private constructor(val value: Int) {
    F2(0), F5(1)
  }

  enum class SPEED private constructor(val value: Float) {
    SPEED1DIV5(1.5f), SPEED2(2.0f), SPEED3(3.0f), SPEED4(4.0f)
  }

  enum class READABLE private constructor(val value: Int) {
    DISABLE(0), EANBLE(1)
  }

  enum class BITMAP_MODE private constructor(val value: Int) {
    OVERWRITE(0), OR(1), XOR(2)
  }

  enum class DENSITY private constructor(val value: Int) {
    DNESITY0(0), DNESITY1(1), DNESITY2(2), DNESITY3(3), DNESITY4(4), DNESITY5(5), DNESITY6(6), DNESITY7(
      7),
    DNESITY8(8), DNESITY9(9), DNESITY10(10), DNESITY11(11), DNESITY12(12), DNESITY13(13), DNESITY14(
      14),
    DNESITY15(15)
  }

  enum class DIRECTION private constructor(val value: Int) {
    FORWARD(0), BACKWARD(1)
  }

  enum class CODEPAGE private constructor(val value: Int) {
    PC437(437), PC850(850), PC852(852), PC860(860), PC863(863), PC865(865), WPC1250(1250), WPC1252(1252), WPC1253(
      1253),
    WPC1254(1254)
  }

  enum class FONTMUL private constructor(val value: Int) {
    MUL_1(1), MUL_2(2), MUL_3(3), MUL_4(4), MUL_5(5), MUL_6(6), MUL_7(7), MUL_8(8), MUL_9(9), MUL_10(10)
  }

  enum class FONTTYPE private constructor(val value: String) {
    FONT_1("1"), FONT_2("2"), FONT_3("3"), FONT_4("4"), FONT_5("5"), FONT_6("6"), FONT_7("7"), FONT_8(
      "8"),
    FONT_CHINESE("TSS24.BF2"), FONT_TAIWAN("TST24.BF2"), FONT_KOREAN("K")
  }

  enum class ROTATION private constructor(val value: Int) {
    ROTATION_0(0), ROTATION_90(90), ROTATION_180(180), ROTATION_270(270)
  }

  enum class BARCODETYPE private constructor(val value: String) {
    CODE128("128"), CODE128M("128M"), EAN128("EAN128"), ITF25("25"), ITF25C("25C"), CODE39("39"), CODE39C(
      "39C"),
    CODE39S("39S"), CODE93("93"), EAN13("EAN13"), EAN13_2("EAN13+2"), EAN13_5("EAN13+5"), EAN8(
      "EAN8"),
    EAN8_2("EAN8+2"), EAN8_5("EAN8+5"), CODABAR("CODA"), POST("POST"), UPCA(
      "EAN13"),
    UPCA_2("EAN13+2"), UPCA_5("EAN13+5"), UPCE("EAN13"), UPCE_2(
      "EAN13+2"),
    UPCE_5("EAN13+5"), CPOST("CPOST"), MSI("MSI"), MSIC(
      "MSIC"),
    PLESSEY("PLESSEY"), ITF14("ITF14"), EAN14("EAN14")
  }

  enum class ENABLE private constructor(val value: String) {
    ON("ON"), OFF("OFF")
  }

  enum class EEC private constructor(val value: String) {
    LEVEL_L("L"),
    LEVEL_M("M"),
    LEVEL_Q("Q"),
    LEVEL_H("H")

  }

  //    public static enum MIRROR {
  //        NORMAL(0), MIRROR(1);
  //        private final int value;
  //        private MIRROR(int value){
  //            this.value = value;
  //        }
  //        public int getValue(){return this.value;}
  //    }

  constructor() {
    this.command = Vector(4096, 1024)
  }

  constructor(width: Int, height: Int, gap: Int) {
    this.command = Vector(4096, 1024)
    addSize(width, height)
    addGap(gap)
  }

  fun clrCommand() {
    this.command!!.clear()
  }

  private fun addStrToCommand(str: String) {
    var bs: ByteArray? = null
    if (str != "") {
      try {
        bs = str.toByteArray(charset("GB2312"))
      } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
      }

      for (i in bs!!.indices) {
        this.command!!.add(java.lang.Byte.valueOf(bs[i]))
      }
    }
  }

  fun addGap(gap: Int) {
    var str = String()
    str = "GAP $gap mm,0 mm\r\n"
    addStrToCommand(str)
  }

  fun addSize(width: Int, height: Int) {
    var str = String()
    str = "SIZE $width mm,$height mm\r\n"
    addStrToCommand(str)
  }

  fun addCashdrwer(m: FOOT, t1: Int, t2: Int) {
    var str = String()
    str = "CASHDRAWER " + m.value + "," + t1 + "," + t2 + "\r\n"
    addStrToCommand(str)
  }

  fun addOffset(offset: Int) {
    var str = String()
    str = "OFFSET $offset mm\r\n"
    addStrToCommand(str)
  }

  fun addSpeed(speed: SPEED) {
    var str = String()
    str = "SPEED " + speed.value + "\r\n"
    addStrToCommand(str)
  }

  fun addDensity(density: DENSITY) {
    var str = String()
    str = "DENSITY " + density.value + "\r\n"
    addStrToCommand(str)
  }

  fun addDirection(direction: DIRECTION) {
    var str = String()
    str = "DIRECTION " + direction.value + "\r\n"
    addStrToCommand(str)
  }

  fun addReference(x: Int, y: Int) {
    var str = String()
    str = "REFERENCE $x,$y\r\n"
    addStrToCommand(str)
  }

  fun addShif(shift: Int) {
    var str = String()
    str = "SHIFT $shift\r\n"
    addStrToCommand(str)
  }

  fun addCls() {
    var str = String()
    str = "CLS\r\n"
    addStrToCommand(str)
  }

  fun addFeed(dot: Int) {
    var str = String()
    str = "FEED $dot\r\n"
    addStrToCommand(str)
  }

  fun addBackFeed(dot: Int) {
    var str = String()
    str = "BACKFEED $dot\r\n"
    addStrToCommand(str)
  }

  fun addFormFeed() {
    var str = String()
    str = "FORMFEED\r\n"
    addStrToCommand(str)
  }

  fun addHome() {
    var str = String()
    str = "HOME\r\n"
    addStrToCommand(str)
  }

  fun addPrint(m: Int, n: Int) {
    var str = String()
    str = "PRINT $m,$n\r\n"
    addStrToCommand(str)
  }

  fun addCodePage(page: CODEPAGE) {
    var str = String()
    str = "CODEPAGE " + page.value + "\r\n"
    addStrToCommand(str)
  }

  fun addSound(level: Int, interval: Int) {
    var str = String()
    str = "SOUND $level,$interval\r\n"
    addStrToCommand(str)
  }

  fun addLimitFeed(n: Int) {
    var str = String()
    str = "LIMITFEED $n\r\n"
    addStrToCommand(str)
  }

  fun addSelfTest() {
    var str = String()
    str = "SELFTEST\r\n"
    addStrToCommand(str)
  }

  fun addBar(x: Int, y: Int, width: Int, height: Int) {
    var str = String()
    str = "BAR $x,$y,$width,$height\r\n"
    addStrToCommand(str)
  }

  fun addText(x: Int, y: Int, font: FONTTYPE, rotation: ROTATION, Xscal: FONTMUL, Yscal: FONTMUL, text: String) {
    var str = String()
    str = ("TEXT " + x + "," + y + "," + "\"" + font.value + "\"" + "," + rotation.value + ","
      + Xscal.value + "," + Yscal.value + "," + "\"" + text + "\"" + "\r\n")
    addStrToCommand(str)
  }

  fun add1DBarcode(x: Int, y: Int, type: BARCODETYPE, height: Int, wide: Int, narrow: Int, readable: READABLE, rotation: ROTATION,
                   content: String) {
    var str = String()
    str = ("BARCODE " + x + "," + y + "," + "\"" + type.value + "\"" + "," + height + "," + readable.value
      + "," + rotation.value + "," + narrow + "," + wide + "," + "\"" + content + "\"" + "\r\n")
    addStrToCommand(str)
  }

  fun addQRCode(x: Int, y: Int, level: EEC, qrWidth: Int, rotation: ROTATION, code: String) {
    //var cmd = 'QRCODE 条码X方向起始点,条码Y方向起始点,纠错级别,二维码高度,A(A和M),旋转角度,M2（分为类型1和类型2）,S1 (s1-s8,默认s7),\"1231你好2421341325454353\"';
    val str = "QRCODE " + x + "," + y + "," + level.value + "," + qrWidth + ",A," + rotation.value + ",M2,S1,\"" + code + "\"\r\n"
    addStrToCommand(str)
  }
  //    public void addBitmap(int x,int y,BITMAP_MODE mode,int imgWidth, Bitmap b){
  //
  //        int width = ((imgWidth + 7) / 8) * 8;
  //        int height = b.getHeight() * width / b.getWidth();
  //        height = ((height + 7) / 8) * 8;
  //
  //        Bitmap rszBitmap = b;
  //        if (b.getWidth() != width) {
  //            rszBitmap = Bitmap.createScaledBitmap(b, width, height, true);
  //        }
  //
  //        Bitmap grayBitmap = PrintPicture.toGrayscale(rszBitmap);
  //        byte[] dithered = PrintPicture.thresholdToBWPic(grayBitmap);
  //        byte[] data =PrintPicture.eachLinePixToCmd(dithered, width, mode.getValue());
  //        height = dithered.length / width;
  //        width /= 8;
  //        //{command} {X},{Y },{width},{ height },{mode},{bitmap data }
  //        //  String str = "BITMAP " + x + "," + y + "," + width + "," + height + "," + mode.getValue() + ",";
  //        String str = "BITMAP "+x+","+y+","+width+","+height+","+mode.getValue()+",";
  //        addStrToCommand(str);
  //        for(int i=0;i<data.length;i++){
  //            Command.add(Byte.valueOf(data[i]));
  //        }
  //        addStrToCommand("\r\n");
  //    }


  fun addBitmap(x: Int, y: Int, mode: TscCommand.BITMAP_MODE, nWidth: Int, b: Bitmap?) {
    if (b != null) {
      var width = (nWidth + 7) / 8 * 8
      var height = b!!.getHeight() * width / b!!.getWidth()
      Log.d("BMP", "bmp.getWidth() " + b!!.getWidth())
      val grayBitmap = PrintPicture.toGrayscale(b)
      val rszBitmap = PrintPicture.resizeImage(grayBitmap, width, height)
      val src = PrintPicture.bitmapToBWPix(rszBitmap)
      height = src.size / width
      width /= 8
      val str = "BITMAP " + x + "," + y + "," + width + "," + height + "," + mode.value + ","
      this.addStrToCommand(str)
      val codecontent = PrintPicture.pixToTscCmd(src)

      for (k in codecontent.indices) {
        this.command!!.add(java.lang.Byte.valueOf(codecontent[k]))
      }

      Log.d("TSCCommand", "codecontent$codecontent")
      addStrToCommand("\r\n")
    }

  }

  fun addBox(x: Int, y: Int, xend: Int, yend: Int) {
    var str = String()
    str = "BAR $x,$y,$xend,$yend\r\n"
    addStrToCommand(str)
  }

  fun addErase(x: Int, y: Int, xwidth: Int, yheight: Int) {
    var str = String()
    str = "ERASE $x,$y,$xwidth,$yheight\r\n"
    addStrToCommand(str)
  }

  fun addReverse(x: Int, y: Int, xwidth: Int, yheight: Int) {
    var str = String()
    str = "REVERSE $x,$y,$xwidth,$yheight\r\n"
    addStrToCommand(str)
  }

  fun queryPrinterType() {
    var str = String()
    str = "~!T\r\n"
    addStrToCommand(str)
  }

  fun queryPrinterStatus() {
    this.command!!.add(java.lang.Byte.valueOf(27.toByte()))
    this.command!!.add(java.lang.Byte.valueOf(33.toByte()))
    this.command!!.add(java.lang.Byte.valueOf(63.toByte()))
  }

  fun resetPrinter() {
    this.command!!.add(java.lang.Byte.valueOf(27.toByte()))
    this.command!!.add(java.lang.Byte.valueOf(33.toByte()))
    this.command!!.add(java.lang.Byte.valueOf(82.toByte()))
  }

  fun queryPrinterLife() {
    var str = String()
    str = "~!@\r\n"
    addStrToCommand(str)
  }

  fun queryPrinterMemory() {
    var str = String()
    str = "~!A\r\n"
    addStrToCommand(str)
  }

  fun queryPrinterFile() {
    var str = String()
    str = "~!F\r\n"
    addStrToCommand(str)
  }

  fun queryPrinterCodePage() {
    var str = String()
    str = "~!I\r\n"
    addStrToCommand(str)
  }

  fun addPeel(enable: ENABLE) {
    var str = String()
    str = "SET PEEL " + enable.value + "\r\n"
    addStrToCommand(str)
  }

  fun addTear(enable: ENABLE) {
    var str = String()
    str = "SET TEAR " + enable.value + "\r\n"
    addStrToCommand(str)
  }

  fun addCutter(enable: ENABLE) {
    var str = String()
    str = "SET CUTTER " + enable.value + "\r\n"
    addStrToCommand(str)
  }

  fun addPartialCutter(enable: ENABLE) {
    var str = String()
    str = "SET PARTIAL_CUTTER " + enable.value + "\r\n"
    addStrToCommand(str)
  }

  companion object {
    private val DEBUG_TAG = "TSCCommand"
  }


}
