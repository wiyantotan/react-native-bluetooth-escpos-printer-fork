package cn.jystudio.bluetooth.escpos.command.sdk

object Command {

  private val ESC: Byte = 0x1B
  private val FS: Byte = 0x1C
  private val GS: Byte = 0x1D
  private val US: Byte = 0x1F
  private val DLE: Byte = 0x10
  private val DC4: Byte = 0x14
  private val DC1: Byte = 0x11
  private val SP: Byte = 0x20
  private val NL: Byte = 0x0A
  private val FF: Byte = 0x0C
  val PIECE = 0xFF.toByte()
  val NUL = 0x00.toByte()

  //打印机初始化
  var ESC_Init = byteArrayOf(ESC, '@'.toByte())

  /**
   * 打印命令
   */
  //打印并换行
  var LF = byteArrayOf(NL)

  //打印并走纸
  var ESC_J = byteArrayOf(ESC, 'J'.toByte(), 0x00)
  var ESC_d = byteArrayOf(ESC, 'd'.toByte(), 0x00)

  //打印自检页
  var US_vt_eot = byteArrayOf(US, DC1, 0x04)

  //蜂鸣指令
  var ESC_B_m_n = byteArrayOf(ESC, 'B'.toByte(), 0x00, 0x00)

  //切刀指令
  var GS_V_n = byteArrayOf(GS, 'V'.toByte(), 0x00)
  var GS_V_m_n = byteArrayOf(GS, 'V'.toByte(), 'B'.toByte(), 0x00)
  var GS_i = byteArrayOf(ESC, 'i'.toByte())
  var GS_m = byteArrayOf(ESC, 'm'.toByte())

  /**
   * 字符设置命令
   */
  //设置字符右间距
  var ESC_SP = byteArrayOf(ESC, SP, 0x00)

  //设置字符打印字体格式
  var ESC_ExclamationMark = byteArrayOf(ESC, '!'.toByte(), 0x00)

  //设置字体倍高倍宽
  var GS_ExclamationMark = byteArrayOf(GS, '!'.toByte(), 0x00)

  //设置反显打印
  var GS_B = byteArrayOf(GS, 'B'.toByte(), 0x00)

  //取消/选择90度旋转打印
  var ESC_V = byteArrayOf(ESC, 'V'.toByte(), 0x00)

  //选择字体字型(主要是ASCII码)
  var ESC_M = byteArrayOf(ESC, 'M'.toByte(), 0x00)

  //选择/取消加粗指令
  var ESC_G = byteArrayOf(ESC, 'G'.toByte(), 0x00)
  var ESC_E = byteArrayOf(ESC, 'E'.toByte(), 0x00)

  //选择/取消倒置打印模式
  var ESC_LeftBrace = byteArrayOf(ESC, '{'.toByte(), 0x00)

  //设置下划线点高度(字符)
  var ESC_Minus = byteArrayOf(ESC, 45, 0x00)

  //字符模式
  var FS_dot = byteArrayOf(FS, 46)

  //汉字模式
  var FS_and = byteArrayOf(FS, '&'.toByte())

  //设置汉字打印模式
  var FS_ExclamationMark = byteArrayOf(FS, '!'.toByte(), 0x00)

  //设置下划线点高度(汉字)
  var FS_Minus = byteArrayOf(FS, 45, 0x00)

  //设置汉字左右间距
  var FS_S = byteArrayOf(FS, 'S'.toByte(), 0x00, 0x00)

  //选择字符代码页
  var ESC_t = byteArrayOf(ESC, 't'.toByte(), 0x00)

  /**
   * 格式设置指令
   */
  //设置默认行间距
  var ESC_Two = byteArrayOf(ESC, 50)

  //设置行间距
  var ESC_Three = byteArrayOf(ESC, 51, 0x00)

  //设置对齐模式
  var ESC_Align = byteArrayOf(ESC, 'a'.toByte(), 0x00)

  //设置左边距
  var GS_LeftSp = byteArrayOf(GS, 'L'.toByte(), 0x00, 0x00)

  //设置绝对打印位置
  //将当前位置设置到距离行首（nL + nH x 256）处。
  //如果设置位置在指定打印区域外，该命令被忽略
  var ESC_Absolute = byteArrayOf(ESC, '$'.toByte(), 0x00, 0x00)

  //设置相对打印位置
  var ESC_Relative = byteArrayOf(ESC, 92, 0x00, 0x00)

  //设置打印区域宽度
  var GS_W = byteArrayOf(GS, 'W'.toByte(), 0x00, 0x00)

  /**
   * 状态指令
   */
  //实时状态传送指令
  var DLE_eot = byteArrayOf(DLE, 0x04, 0x00)

  //实时弹钱箱指令
  var DLE_DC4 = byteArrayOf(DLE, DC4, 0x00, 0x00, 0x00)

  //标准弹钱箱指令
  var ESC_p = byteArrayOf(ESC, 'p'.toByte(), 0x00, 0x00, 0x00)

  /**
   * 条码设置指令
   */
  //选择HRI打印方式
  var GS_H = byteArrayOf(GS, 'H'.toByte(), 0x00)

  //设置条码高度
  var GS_h = byteArrayOf(GS, 'h'.toByte(), 0xa2.toByte())

  //设置条码宽度
  var GS_w = byteArrayOf(GS, 'w'.toByte(), 0x00)

  //设置HRI字符字体字型
  var GS_f = byteArrayOf(GS, 'f'.toByte(), 0x00)

  //条码左偏移指令
  var GS_x = byteArrayOf(GS, 'x'.toByte(), 0x00)

  //打印条码指令
  var GS_k = byteArrayOf(GS, 'k'.toByte(), 'A'.toByte(), FF)

  //二维码相关指令
  var GS_k_m_v_r_nL_nH = byteArrayOf(ESC, 'Z'.toByte(), 0x03, 0x03, 0x08, 0x00, 0x00)

}
