package com.example.tixngo_manager.tixngo

import android.content.Context
import android.content.Intent
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor.DartEntrypoint
import io.flutter.plugin.common.MethodChannel


class TixngoSdk (context: Context) {

    private val flutterEngine = FlutterEngine(context);

    private val channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "io.tixngo.sdk")

    fun initialize(handler: MethodChannel.MethodCallHandler) {
        flutterEngine.dartExecutor.executeDartEntrypoint(DartEntrypoint.createDefault())
        FlutterEngineCache.getInstance().put("engine", flutterEngine)
        channel.setMethodCallHandler(handler)
    }

    fun rootIntent(context: Context): Intent {
        return FlutterActivity.withCachedEngine("engine").build(context)
    }

    fun sendMessage(method: String, arguments: Any?, completion: ((Any?) -> Unit?)? = null) {
        channel.invokeMethod(method, arguments, object: MethodChannel.Result {
            override fun success(result: Any?) {
                if (completion != null) {
                    completion(result)
                }
            }

            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                if (completion != null) {
                    completion(errorMessage + errorCode)
                }
            }

            override fun notImplemented() {
                if (completion != null) {
                    completion("Not implemented")
                }
            }

        })
    }

}