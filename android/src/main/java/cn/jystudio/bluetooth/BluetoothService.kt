package cn.jystudio.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.lang.reflect.Method

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices.
 */
class BluetoothService
/**
 * Constructor. Prepares a new BTPrinter session.
 *
 * @param context The UI Activity Context
 */
(context: Context) {

  // Member fields
  private val mAdapter: BluetoothAdapter

  private var mConnectedThread: ConnectedThread? = null
  /**
   * Return the current connection state.
   */
  //todo: get the method in react to check the current connection state
  var state: Int = 0
    @Synchronized get
    private @Synchronized set
  //Method to get the address of the last connected device
  var lastConnectedDeviceAddress = ""
    private set

  init {
    mAdapter = BluetoothAdapter.getDefaultAdapter()
    state = STATE_NONE

  }

  fun addStateObserver(observer: BluetoothServiceStateObserver) {
    observers.add(observer)
  }

  fun removeStateObserver(observer: BluetoothServiceStateObserver) {
    observers.remove(observer)
  }

  /**
   * Set the current state of the connection
   *
   * @param state An integer defining the current connection state
   */
  @Synchronized
  private fun setState(state: Int, bundle: Map<String, Any>?) {
    if (DEBUG) Log.d(TAG, "setState() " + getStateName(this.state) + " -> " + getStateName(state))
    this.state = state
    infoObervers(state, bundle)
  }

  private fun getStateName(state: Int): String {
    var name = "UNKNOW:$state"
    if (STATE_NONE == state) {
      name = "STATE_NONE"
    } else if (STATE_CONNECTED == state) {
      name = "STATE_CONNECTED"
    } else if (STATE_CONNECTING == state) {
      name = "STATE_CONNECTING"
    }
    return name
  }

  @Synchronized
  private fun infoObervers(code: Int, bundle: Map<String, Any>?) {
    for (ob in observers) {
      ob.onBluetoothServiceStateChanged(code, bundle)
    }
  }


  /**
   * Start the ConnectThread to initiate a connection to a remote device.
   *
   * @param device The BluetoothDevice to connect
   */
  @Synchronized
  fun connect(device: BluetoothDevice) {
    if (DEBUG) Log.d(TAG, "connect to: $device")
    var connectedDevice: BluetoothDevice? = null
    if (mConnectedThread != null) {
      connectedDevice = mConnectedThread!!.bluetoothDevice()
    }
    if (state == STATE_CONNECTED && connectedDevice != null && connectedDevice!!.getAddress().equals(device.getAddress())) {
      // connected already
      val bundle = HashMap<String, Any>()
      bundle.put(DEVICE_NAME, device.getName())
      bundle.put(DEVICE_ADDRESS, device.getAddress())
      setState(STATE_CONNECTED, bundle)
    } else {
      // Cancel any thread currently running a connection
      this.stop()
      // Start the thread to manage the connection and perform transmissions
      mConnectedThread = ConnectedThread(device)
      mConnectedThread!!.start()
      setState(STATE_CONNECTING, null)
    }
  }

  /**
   * Stop all threads
   */
  @Synchronized
  fun stop() {
    if (mConnectedThread != null) {
      mConnectedThread!!.cancel()
      mConnectedThread = null
    }
  }

  /**
   * Write to the ConnectedThread in an unsynchronized manner
   *
   * @param out The bytes to write
   * @see ConnectedThread.write
   */
  fun write(out: ByteArray) {
    // Create temporary object
    var r: ConnectedThread?
    // Synchronize a copy of the ConnectedThread
    synchronized(this) {
      if (state != STATE_CONNECTED) return
      r = mConnectedThread
    }
    r!!.write(out)
  }

  /**
   * Indicate that the connection attempt failed.
   */
  private fun connectionFailed() {
    setState(STATE_NONE, null)
    infoObervers(MESSAGE_UNABLE_CONNECT, null)
  }

  /**
   * Indicate that the connection was lost and notify the UI Activity.
   */
  private fun connectionLost() {
    setState(STATE_NONE, null)
    infoObervers(MESSAGE_CONNECTION_LOST, null)
  }

  /**
   * This thread runs during a connection with a remote device.
   * It handles all incoming and outgoing transmissions.
   */
  private inner class ConnectedThread(private val mmDevice: BluetoothDevice) : Thread() {
    private var mmSocket: BluetoothSocket? = null
    private var mmInStream: InputStream? = null
    private var mmOutStream: OutputStream? = null

    init {
      mmDevice.getAddress()
    }

    override fun run() {
      Log.i(TAG, "BEGIN mConnectThread")
      setName("ConnectThread")
      var bundle: MutableMap<String, Any> = HashMap<String, Any>()

      // Always cancel discovery because it will slow down a connection
      mAdapter.cancelDiscovery()

      var tmp: BluetoothSocket? = null

      // try to connect with socket inner method firstly.
      for (i in 1..3) {
        try {
//          tmp = mmDevice.getClass().getMethod("createRfcommSocket", Int::class.javaPrimitiveType).invoke(mmDevice, i) as BluetoothSocket
          tmp = mmDevice.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType).invoke(mmDevice, i) as BluetoothSocket
        } catch (e: Exception) {
        }

        if (tmp != null) {
          mmSocket = tmp
          break
        }
      }

      // try with given uuid
      if (mmSocket == null) {
        try {
          tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID)
        } catch (e: IOException) {
          e.printStackTrace()
          Log.e(TAG, "create() failed", e)
        }

        if (tmp == null) {
          Log.e(TAG, "create() failed: Socket NULL.")
          connectionFailed()
          return
        }
      }
      mmSocket = tmp

      // Make a connection to the BluetoothSocket
      try {
        // This is a blocking call and will only return on a
        // successful connection or an exception
        mmSocket!!.connect()
      } catch (e: Exception) {
        e.printStackTrace()
        connectionFailed()
        // Close the socket
        try {
          mmSocket!!.close()
        } catch (e2: Exception) {
          Log.e(TAG, "unable to close() socket during connection failure", e2)
        }

        return
      }


      Log.d(TAG, "create ConnectedThread")
      var tmpIn: InputStream? = null
      var tmpOut: OutputStream? = null

      // Get the BluetoothSocket input and output streams
      try {
        tmpIn = mmSocket!!.getInputStream()
        tmpOut = mmSocket!!.getOutputStream()
      } catch (e: IOException) {
        Log.e(TAG, "temp sockets not created", e)
      }

      mmInStream = tmpIn
      mmOutStream = tmpOut

      bundle[DEVICE_NAME] = mmDevice.getName()
      bundle[DEVICE_ADDRESS] = mmDevice.getAddress()
      setState(STATE_CONNECTED, bundle)

      Log.i(TAG, "Connected")
      var bytes: Int

      //keep the address of last connected device and get this address directly in the .js code
      lastConnectedDeviceAddress = mmDevice.getAddress()

      // Keep listening to the InputStream while connected
      while (true) {
        try {
          val buffer = ByteArray(256)
          // Read from the InputStream
          bytes = mmInStream!!.read(buffer)
          if (bytes > 0) {
            // Send the obtained bytes to the UI Activity
            bundle = HashMap<String, Any>()
            bundle["bytes"] = bytes
            infoObervers(MESSAGE_READ, bundle)
          } else {
            Log.e(TAG, "disconnected")
            connectionLost()
            break
          }
        } catch (e: IOException) {
          Log.e(TAG, "disconnected", e)
          connectionLost()
          break
        }

      }
      Log.i(TAG, "ConnectedThread End")
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    fun write(buffer: ByteArray) {
      try {
        mmOutStream!!.write(buffer)
        mmOutStream!!.flush()//清空缓存
        /* if (buffer.length > 3000) //
                {
                  byte[] readata = new byte[1];
                  SPPReadTimeout(readata, 1, 5000);
                }*/
        Log.i("BTPWRITE", String(buffer, charset("GBK")))
        val bundle = HashMap<String, Any>()
        bundle.put("bytes", buffer)
        infoObervers(MESSAGE_WRITE, bundle)
      } catch (e: IOException) {
        Log.e(TAG, "Exception during write", e)
      }

    }

    fun bluetoothDevice(): BluetoothDevice? {
      return if (mmSocket != null && mmSocket!!.isConnected()) {
        mmSocket!!.getRemoteDevice()
      } else {
        null
      }
    }

    fun cancel() {
      try {
        mmSocket!!.close()
        connectionLost()
      } catch (e: IOException) {
        Log.e(TAG, "close() of connect socket failed", e)
      }

    }
  }

  companion object {
    // Debugging
    private val TAG = "BluetoothService"
    private val DEBUG = true


    // Name for the SDP record when creating server socket
    private val NAME = "BTPrinter"
    //UUID must be this
    // Unique UUID for this application
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Constants that indicate the current connection state
    val STATE_NONE = 0       // we're doing nothing
    // public static final int STATE_LISTEN = 1;     // now listening for incoming connections //feathure removed.
    val STATE_CONNECTING = 2 // now initiating an outgoing connection
    val STATE_CONNECTED = 3  // now connected to a remote device


    val MESSAGE_STATE_CHANGE = 4
    val MESSAGE_READ = 5
    val MESSAGE_WRITE = 6
    val MESSAGE_DEVICE_NAME = 7
    val MESSAGE_CONNECTION_LOST = 8
    val MESSAGE_UNABLE_CONNECT = 9

    // Key names received from the BluetoothService Handler
    val DEVICE_NAME = "device_name"
    val DEVICE_ADDRESS = "device_address"
    val TOAST = "toast"

    var ErrorMessage = "No_Error_Message"

    private val observers = ArrayList<BluetoothServiceStateObserver>()
  }
}
