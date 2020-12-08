package cn.jystudio.bluetooth


import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.Toast

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

import org.json.JSONArray
import org.json.JSONObject

import java.lang.reflect.Method
import java.util.Collections
import java.util.HashMap

/**
 * Created by januslo on 2018/9/22.
 */
class RNBluetoothManagerModule(private val reactContext: ReactApplicationContext, bluetoothService: BluetoothService) : ReactContextBaseJavaModule(reactContext), ActivityEventListener, BluetoothServiceStateObserver {

  private var pairedDeivce = JSONArray()
  private var foundDevice = JSONArray()
  // Name of the connected device
  private var mConnectedDeviceName: String? = null
  // Local Bluetooth adapter
  private var mBluetoothAdapter: BluetoothAdapter? = null
  // Member object for the services
  private var mService: BluetoothService = bluetoothService

  override fun getConstants() : Map<String, Any> {
      val constants = HashMap<String, Any>()
      constants.put(EVENT_DEVICE_ALREADY_PAIRED, EVENT_DEVICE_ALREADY_PAIRED)
      constants.put(EVENT_DEVICE_DISCOVER_DONE, EVENT_DEVICE_DISCOVER_DONE)
      constants.put(EVENT_DEVICE_FOUND, EVENT_DEVICE_FOUND)
      constants.put(EVENT_CONNECTION_LOST, EVENT_CONNECTION_LOST)
      constants.put(EVENT_UNABLE_CONNECT, EVENT_UNABLE_CONNECT)
      constants.put(EVENT_CONNECTED, EVENT_CONNECTED)
      constants.put(EVENT_BLUETOOTH_NOT_SUPPORT, EVENT_BLUETOOTH_NOT_SUPPORT)
      constants.put(DEVICE_NAME, DEVICE_NAME)
      constants.put(EVENT_BLUETOOTH_NOT_SUPPORT, EVENT_BLUETOOTH_NOT_SUPPORT)
      return constants
  }

  private// Get local Bluetooth adapter
  // If the adapter is null, then Bluetooth is not supported
  val bluetoothAdapter: BluetoothAdapter?
    get() {
      if (mBluetoothAdapter == null) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
      }
      if (mBluetoothAdapter == null) {
        emitRNEvent(EVENT_BLUETOOTH_NOT_SUPPORT, Arguments.createMap())
      }

      return mBluetoothAdapter
    }

  override fun getName(): String {
    return "BluetoothManager"
  }

  // The BroadcastReceiver that listens for discovered devices and
  // changes the title when discovery is finished
  private val discoverReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.getAction()
      Log.d(TAG, "on receive:$action")
      // When discovery finds a device
      if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        // Get the BluetoothDevice object from the Intent
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        if (device.getBondState() !== BluetoothDevice.BOND_BONDED) {
          val deviceFound = JSONObject()
          try {
            deviceFound.put("name", device.getName())
            deviceFound.put("address", device.getAddress())
          } catch (e: Exception) {
            //ignore
          }

          if (!objectFound(deviceFound)) {
            foundDevice.put(deviceFound)
            val params = Arguments.createMap()
            params.putString("device", deviceFound.toString())
            emitRNEvent(EVENT_DEVICE_FOUND, params)
          }

        }
      } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        val promise = promiseMap.remove(PROMISE_SCAN)
        if (promise != null) {

          var result: JSONObject? = null
          try {
            result = JSONObject()
            result!!.put("paired", pairedDeivce)
            result!!.put("found", foundDevice)
            promise!!.resolve(result!!.toString())
          } catch (e: Exception) {
            //ignore
          }

          val params = Arguments.createMap()
          params.putString("paired", pairedDeivce.toString())
          params.putString("found", foundDevice.toString())
          emitRNEvent(EVENT_DEVICE_DISCOVER_DONE, params)
        }
      }
    }
  }

  init {
    this.reactContext.addActivityEventListener(this)
    this.mService = bluetoothService
    this.mService!!.addStateObserver(this)
    // Register for broadcasts when a device is discovered
    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    this.reactContext.registerReceiver(discoverReceiver, filter)
  }


  @ReactMethod
  fun enableBluetooth(promise: Promise) {
    val adapter = this.bluetoothAdapter
    if (adapter == null) {
      promise.reject(EVENT_BLUETOOTH_NOT_SUPPORT, EVENT_BLUETOOTH_NOT_SUPPORT)
    } else if (!adapter!!.isEnabled()) {
      // If Bluetooth is not on, request that it be enabled.
      // setupChat() will then be called during onActivityResult
      val enableIntent = Intent(
        BluetoothAdapter.ACTION_REQUEST_ENABLE)
      promiseMap.put(PROMISE_ENABLE_BT, promise)
      this.reactContext.startActivityForResult(enableIntent, REQUEST_ENABLE_BT, Bundle.EMPTY)
    } else {
      val pairedDeivce = Arguments.createArray()
      val boundDevices = adapter!!.getBondedDevices()
      for (d in boundDevices) {
        try {
          val obj = JSONObject()
          obj.put("name", d.getName())
          obj.put("address", d.getAddress())
          pairedDeivce.pushString(obj.toString())
        } catch (e: Exception) {
          //ignore.
        }

      }
      Log.d(TAG, "ble Enabled")
      promise.resolve(pairedDeivce)
    }
  }

  @ReactMethod
  fun disableBluetooth(promise: Promise) {
    val adapter = this.bluetoothAdapter
    if (adapter == null) {
      promise.resolve(true)
    } else {
      if (mService != null && mService!!.state !== BluetoothService.STATE_NONE) {
        mService!!.stop()
      }
      promise.resolve(!adapter!!.isEnabled() || adapter!!.disable())
    }
  }

  @ReactMethod
  fun isBluetoothEnabled(promise: Promise) {
    val adapter = this.bluetoothAdapter
    promise.resolve(adapter != null && adapter!!.isEnabled())
  }

  @ReactMethod
  fun stopScan(promise: Promise) {
    cancelDisCovery()
    promise.resolve(true)
  }

  @ReactMethod
  fun scanDevices(promise: Promise) {
    val adapter = this.bluetoothAdapter
    if (adapter == null) {
      promise.reject(EVENT_BLUETOOTH_NOT_SUPPORT, EVENT_BLUETOOTH_NOT_SUPPORT)
    } else {
      cancelDisCovery()
      val permissionChecked = ContextCompat.checkSelfPermission(reactContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
      if (permissionChecked == PackageManager.PERMISSION_DENIED) {
        // // TODO: 2018/9/21
        ActivityCompat.requestPermissions(reactContext.getCurrentActivity()!!,
          arrayOf<String>(android.Manifest.permission.ACCESS_COARSE_LOCATION),
          1)
      }


      pairedDeivce = JSONArray()
      foundDevice = JSONArray()
      val boundDevices = adapter!!.getBondedDevices()
      for (d in boundDevices) {
        try {
          val obj = JSONObject()
          obj.put("name", d.getName())
          obj.put("address", d.getAddress())
          pairedDeivce.put(obj)
        } catch (e: Exception) {
          //ignore.
        }

      }

      val params = Arguments.createMap()
      params.putString("devices", pairedDeivce.toString())
      emitRNEvent(EVENT_DEVICE_ALREADY_PAIRED, params)
      if (!adapter!!.startDiscovery()) {
        promise.reject("DISCOVER", "NOT_STARTED")
        cancelDisCovery()
      } else {
        promiseMap.put(PROMISE_SCAN, promise)
      }
    }
  }

  @ReactMethod
  fun connect(address: String, promise: Promise) {
    val adapter = this.bluetoothAdapter
    if (adapter == null) {
      promise.reject(EVENT_BLUETOOTH_NOT_SUPPORT, EVENT_BLUETOOTH_NOT_SUPPORT)
    } else if(!adapter!!.isEnabled()) {
      promise.reject(EVENT_BLUETOOTH_NOT_ENABLED, EVENT_BLUETOOTH_NOT_ENABLED)
    } else {
      val device = adapter!!.getRemoteDevice(address)
      promiseMap.put(PROMISE_CONNECT, promise)
      mService!!.connect(device)
    }
  }

  @ReactMethod
  fun disconnect(address: String, promise: Promise) {
    val adapter = this.bluetoothAdapter
    if (adapter == null) {
      promise.reject(EVENT_BLUETOOTH_NOT_SUPPORT, EVENT_BLUETOOTH_NOT_SUPPORT)
    } else if(!adapter!!.isEnabled()) {
      promise.reject(EVENT_BLUETOOTH_NOT_ENABLED, EVENT_BLUETOOTH_NOT_ENABLED)
    } else {
      val device = adapter!!.getRemoteDevice(address)
      try {
        mService!!.stop()
      } catch (e: Exception) {
        Log.e(TAG, e.message)
      }

      promise.resolve(address)
    }
  }

  @ReactMethod
  fun unpaire(address: String, promise: Promise) {
    val adapter = this.bluetoothAdapter
    if (adapter == null) {
      promise.reject(EVENT_BLUETOOTH_NOT_SUPPORT, EVENT_BLUETOOTH_NOT_SUPPORT)
    } else if(!adapter!!.isEnabled()) {
      promise.reject(EVENT_BLUETOOTH_NOT_ENABLED, EVENT_BLUETOOTH_NOT_ENABLED)
    } else {
      val device = adapter!!.getRemoteDevice(address)
      this.unpairDevice(device)
      promise.resolve(address)
    }
  }


  /*
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    // public static final int STATE_LISTEN = 1;     // now listening for incoming connections //feathure removed.
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
*/
  @ReactMethod
  fun isDeviceConnected(promise: Promise) {

    var isConnected: Boolean? = true

    if (mService != null) {
      when (mService.state) {
        0 -> isConnected = false

        2 -> isConnected = false

        3 -> isConnected = true

        else -> isConnected = false
      }
      promise.resolve(isConnected)
    } else {
      promise.reject(EVENT_BLUETOOTH_NOT_SUPPORT, EVENT_BLUETOOTH_NOT_SUPPORT)
    }
  }


  /* Return the address of the currently connected device */
  @ReactMethod
  fun getConnectedDeviceAddress(promise: Promise) {
    if (mService != null) {
      promise.resolve(mService!!.lastConnectedDeviceAddress)
    } else {
      promise.reject(EVENT_BLUETOOTH_NOT_SUPPORT, EVENT_BLUETOOTH_NOT_SUPPORT)
    }

  }


  private fun unpairDevice(device: BluetoothDevice) {
    try {
      val m = device.javaClass
        .getMethod("removeBond", null as Class<*>)
      m.invoke(device, null as Array<Any>?)
    } catch (e: Exception) {
      Log.e(TAG, e.message)
    }

  }

  private fun cancelDisCovery() {
    try {
      val adapter = this.bluetoothAdapter
      if (adapter != null && adapter!!.isDiscovering()) {
        adapter!!.cancelDiscovery()
      }
      Log.d(TAG, "Discover canceled")
    } catch (e: Exception) {
      //ignore
    }

  }


  override fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
    Log.d(TAG, "onActivityResultBluetooth $resultCode - $requestCode")
    val adapter = this.bluetoothAdapter
    when (requestCode) {
      REQUEST_CONNECT_DEVICE -> {
        // When DeviceListActivity returns with a device to connect
        if (resultCode == Activity.RESULT_OK) {
          // Get the device MAC address
          if (data != null) {
            val address = data.getExtras().getString(
              EXTRA_DEVICE_ADDRESS)
            // Get the BLuetoothDevice object
            if (adapter != null && BluetoothAdapter.checkBluetoothAddress(address)) {
              val device = adapter!!
                .getRemoteDevice(address)
              // Attempt to connect to the device
              mService!!.connect(device)
            }
          }
        }
      }
      REQUEST_ENABLE_BT -> {
        val promise = promiseMap.remove(PROMISE_ENABLE_BT)
        // When the request to enable Bluetooth returns
        if (resultCode == Activity.RESULT_OK && promise != null) {
          // Bluetooth is now enabled, so set up a session
          if (adapter != null) {
            val pairedDeivce = Arguments.createArray()
            val boundDevices = adapter!!.getBondedDevices()
            for (d in boundDevices) {
              try {
                val obj = JSONObject()
                obj.put("name", d.getName())
                obj.put("address", d.getAddress())
                pairedDeivce.pushString(obj.toString())
              } catch (e: Exception) {
                //ignore.
              }

            }
            promise!!.resolve(pairedDeivce)
          } else {
            promise!!.resolve(null)
          }

        } else {
          // User did not enable Bluetooth or an error occured
          Log.d(TAG, "BT not enabled")
          if (promise != null) {
            promise!!.reject("EVENT_BLUETOOTH_NOT_ENABLED", EVENT_BLUETOOTH_NOT_ENABLED)
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {

  }


  private fun objectFound(obj: JSONObject): Boolean {
    var found = false
    if (foundDevice.length() > 0) {
      for (i in 0 until foundDevice.length()) {
        try {
          val objAddress = obj.optString("address", "objAddress")
          val dsAddress = (foundDevice.get(i) as JSONObject).optString("address", "dsAddress")
          if (objAddress.equals(dsAddress, ignoreCase = true)) {
            found = true
            break
          }
        } catch (e: Exception) {
        }

      }
    }
    return found
  }

  private fun emitRNEvent(event: String, params: WritableMap?) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(event, params)
  }

  override fun onBluetoothServiceStateChanged(state: Int, bundle: Map<String, Any>?) {
    Log.d(TAG, "on bluetoothServiceStatChange:$state")
    when (state) {
      BluetoothService.STATE_CONNECTED, MESSAGE_DEVICE_NAME -> {
        // save the connected device's name
        if (bundle != null) {
          mConnectedDeviceName = bundle[DEVICE_NAME] as String
          val p = promiseMap.remove(PROMISE_CONNECT)
          if (p == null) {
            Log.d(TAG, "No Promise found.")
            val params = Arguments.createMap()
            params.putString(DEVICE_NAME, mConnectedDeviceName)
            emitRNEvent(EVENT_CONNECTED, params)
          } else {
            Log.d(TAG, "Promise Resolve.")
            p!!.resolve(mConnectedDeviceName)
          }
        }
      }
      MESSAGE_CONNECTION_LOST -> {
        //Connection lost should not be the connect result.
        // Promise p = promiseMap.remove(PROMISE_CONNECT);
        // if (p == null) {
        emitRNEvent(EVENT_CONNECTION_LOST, null)
      }// } else {
      //   p.reject("Device connection was lost");
      //}
      MESSAGE_UNABLE_CONNECT -> {     //无法连接设备
        val p = promiseMap.remove(PROMISE_CONNECT)
        if (p == null) {
          emitRNEvent(EVENT_UNABLE_CONNECT, null)
        } else {
          p!!.reject("Unable to connect device")
        }
      }
      else -> {
      }
    }
  }

  companion object {

    private val TAG = "BluetoothManager"
    val EVENT_DEVICE_ALREADY_PAIRED = "EVENT_DEVICE_ALREADY_PAIRED"
    val EVENT_DEVICE_FOUND = "EVENT_DEVICE_FOUND"
    val EVENT_DEVICE_DISCOVER_DONE = "EVENT_DEVICE_DISCOVER_DONE"
    val EVENT_CONNECTION_LOST = "EVENT_CONNECTION_LOST"
    val EVENT_UNABLE_CONNECT = "EVENT_UNABLE_CONNECT"
    val EVENT_CONNECTED = "EVENT_CONNECTED"
    val EVENT_BLUETOOTH_NOT_SUPPORT = "EVENT_BLUETOOTH_NOT_SUPPORT"
    val EVENT_BLUETOOTH_NOT_ENABLED = "EVENT_BLUETOOTH_NOT_ENABLED"



    // Intent request codes
    private val REQUEST_CONNECT_DEVICE = 1
    private val REQUEST_ENABLE_BT = 2

    val MESSAGE_STATE_CHANGE = BluetoothService.MESSAGE_STATE_CHANGE
    val MESSAGE_READ = BluetoothService.MESSAGE_READ
    val MESSAGE_WRITE = BluetoothService.MESSAGE_WRITE
    val MESSAGE_DEVICE_NAME = BluetoothService.MESSAGE_DEVICE_NAME

    val MESSAGE_CONNECTION_LOST = BluetoothService.MESSAGE_CONNECTION_LOST
    val MESSAGE_UNABLE_CONNECT = BluetoothService.MESSAGE_UNABLE_CONNECT
    val DEVICE_NAME = BluetoothService.DEVICE_NAME
    val TOAST = BluetoothService.TOAST

    // Return Intent extra
    var EXTRA_DEVICE_ADDRESS = "device_address"

    private val promiseMap = Collections.synchronizedMap(HashMap<String, Promise>())
    private val PROMISE_ENABLE_BT = "ENABLE_BT"
    private val PROMISE_SCAN = "SCAN"
    private val PROMISE_CONNECT = "CONNECT"
  }
}
