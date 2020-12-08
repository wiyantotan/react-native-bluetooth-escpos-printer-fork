package cn.jystudio.bluetooth


/**
 * Created by januslo on 2018/9/22.
 */
interface BluetoothServiceStateObserver {
  fun onBluetoothServiceStateChanged(state: Int, boundle: Map<String, Any>?)
}
