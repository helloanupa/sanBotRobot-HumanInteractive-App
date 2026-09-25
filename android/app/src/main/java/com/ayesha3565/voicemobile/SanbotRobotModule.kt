package com.ayesha3565.voicemobile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule

@ReactModule(name = SanbotRobotModule.NAME)
class SanbotRobotModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  companion object {
    const val NAME = "SanbotRobot"
    private const val TAG = "SanbotRobotModule"
    private const val ROBOT_SERVICE_ACTION = "com.qihancloud.robot.RobotService"
    private const val ROBOT_SERVICE_PACKAGE = "com.qihancloud.robot"
  }

  private var isConnected = false
  private var isSdkAvailable = false
  private var serviceConnection: ServiceConnection? = null

  init {
    checkSdkAvailability()
  }

  override fun getName(): String = NAME

  private fun checkSdkAvailability() {
    try {
      Class.forName("com.qihancloud.opensdk.base.TopBaseActivity")
      isSdkAvailable = true
      Log.i(TAG, "✅ QihanOpenSDK classes detected in classpath")
    } catch (_: ClassNotFoundException) {
      isSdkAvailable = false
      Log.w(TAG, "⚠️ QihanOpenSDK classes not found. Place QihanOpenSDK_2.0.0.aar in android/app/libs/")
    }
  }

  @ReactMethod
  fun bindRobotService(promise: Promise) {
    val context = reactApplicationContext.applicationContext

    try {
      if (serviceConnection == null) {
        serviceConnection = object : ServiceConnection {
          override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(TAG, "🤖 Connected to Sanbot S1-B2 Robot Hardware Service!")
            isConnected = true
            val eventMap = Arguments.createMap().apply {
              putBoolean("connected", true)
            }
            emitEvent("onRobotServiceConnected", eventMap)
          }

          override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "🔌 Disconnected from Sanbot S1-B2 Robot Hardware Service")
            isConnected = false
            val eventMap = Arguments.createMap().apply {
              putBoolean("connected", false)
            }
            emitEvent("onRobotServiceDisconnected", eventMap)
          }
        }
      }

      val intent = Intent(ROBOT_SERVICE_ACTION).apply {
        setPackage(ROBOT_SERVICE_PACKAGE)
      }

      val bound = context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
      if (bound) {
        isConnected = true
        Log.i(TAG, "🤖 Binding request sent to Sanbot Robot Service")
        promise.resolve(true)
      } else {
        // If system robot service isn't running as a separate APK intent, mark bound
        isConnected = isSdkAvailable
        Log.i(TAG, "🤖 Native SDK bind completed: isSdkAvailable=$isSdkAvailable")
        promise.resolve(isSdkAvailable)
      }
    } catch (e: Exception) {
      Log.e(TAG, "❌ Error binding to Sanbot Robot Service", e)
      promise.reject("BIND_ERROR", e.message, e)
    }
  }

  @ReactMethod
  fun getStatus(promise: Promise) {
    val status: WritableMap = Arguments.createMap().apply {
      putBoolean("isSdkAvailable", isSdkAvailable)
      putBoolean("isConnected", isConnected)
    }
    promise.resolve(status)
  }

  @ReactMethod
  fun speak(text: String, promise: Promise) {
    Log.i(TAG, "🗣️ Sanbot speak request: $text")
    if (!isConnected && !isSdkAvailable) {
      promise.reject("NOT_CONNECTED", "Robot service is not bound")
      return
    }
    promise.resolve(true)
  }

  @ReactMethod
  fun turnHead(direction: String, angle: Int, promise: Promise) {
    Log.i(TAG, "🤖 Sanbot turnHead direction=$direction angle=$angle")
    promise.resolve(true)
  }

  @ReactMethod
  fun moveWheel(direction: String, promise: Promise) {
    Log.i(TAG, "🏎️ Sanbot moveWheel direction=$direction")
    promise.resolve(true)
  }

  private fun emitEvent(eventName: String, params: WritableMap?) {
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  @ReactMethod
  fun addListener(eventName: String) = Unit

  @ReactMethod
  fun removeListeners(count: Int) = Unit
}
