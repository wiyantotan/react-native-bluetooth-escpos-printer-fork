package cn.jystudio.bluetooth

import java.util.Arrays
import java.util.Collections

import cn.jystudio.bluetooth.escpos.RNBluetoothEscposPrinterModule
import cn.jystudio.bluetooth.tsc.RNBluetoothTscPrinterModule
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager
import com.facebook.react.bridge.JavaScriptModule

class RNBluetoothEscposPrinterPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    val service = BluetoothService(reactContext)
    return Arrays.asList<NativeModule>(RNBluetoothManagerModule(reactContext, service),
      RNBluetoothEscposPrinterModule(reactContext, service),
      RNBluetoothTscPrinterModule(reactContext, service))
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return emptyList<ViewManager<*, *>>()
  }
}
