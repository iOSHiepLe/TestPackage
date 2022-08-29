package com.example.tixngo_manager.tixngo

import TixngoEnv
import TixngoProfile
import TixngoPushNotification
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper

class TixngoManager {

    companion object {
        private var ins: TixngoManager? = null
        val instance: TixngoManager
            get() {
                if (ins == null) {
                    ins = TixngoManager()
                }
                return ins!!
            }
    }

    private lateinit var sdk: TixngoSdk

    // ----- Request: Native --> Sdk -----//
    private val openHomePage = "sdk.open_home_page"
    private val openEventPage = "sdk.open_my_event_page"
    private val openTransferredPage = "sdk.open_transferred_ticket_page"
    private val openPendingPage = "sdk.open_pending_ticket_page"
    private val doSetProfile = "sdk.set_profile"
    private val doSetEnv = "sdk.set_environment"
    private val doSignOut = "sdk.do_sign_out"
    private val doSignIn = "sdk.do_sign_in"
    private val getAuthStatus = "sdk.get_auth_status"
    private val processPushMessage = "sdk.process_push_message"
    private val processPushDeviceToken = "sdk.process_push_device_token"

    // ----- Request: Sdk --> Native -----//
    private val onTokenExpired = "sdk.on_token_expired"
    private val onCloseButton = "sdk.on_close_button_pressed"
    private val onGetJwtToken = "sdk.get_jwt_token"
    private val onGetDeviceToken = "sdk.get_device_token"
    private val onInitialized = "sdk.initialized"
    private val onDebug = "sdk.debug"
    private var hostActivity: Activity? = null

    /*
     Call this method to initialize SDK, and received isAuthenticated status in onInitializedHandler
     + onInitializedHandler: SDK return isAuthenticated to app
     + onTokenExpiredHandler: SDK request shouldRetry from app, if true, app should refresh token in advance
     + onGetJwtTokenHandler: SDK request jwtToken from app, it will be called whenever SDK request data from backend and during initialization
     + onGetDeviceTokenHandler: SDK request fcmDeviceToken from app
     + onCloseHandler: SDK notify app that user tap close button, app should close SDK UI and return to app UI
    */
    fun initialize(
        context: Context,
        onInitializedHandler: ((Boolean) -> Unit),
        onTokenExpiredHandler: (((Boolean) -> Unit) -> Unit),
        onGetJwtTokenHandler: (((String?) -> Unit) -> Unit),
        onGetDeviceTokenHandler: (((String?) -> Unit) -> Unit),
        onCloseHandler: () -> Unit,
    ) {
        sdk = TixngoSdk(context)
        sdk.initialize { call, result ->
            val method = call.method
            val args = call.arguments
            when (method) {
                onInitialized -> {
                    onInitializedHandler(args as Boolean)
                    result.success(null)
                }
                onGetJwtToken -> {
                   onGetJwtTokenHandler { token ->
                       Handler(Looper.getMainLooper()).post {
                           result.success(token)
                       }
                   }
                }
                onGetDeviceToken -> {
                    onGetDeviceTokenHandler { token ->
                        Handler(Looper.getMainLooper()).post {
                            result.success(token)
                        }
                    }
                }
                onTokenExpired -> {
                    onTokenExpiredHandler { shouldRetry ->
                        Handler(Looper.getMainLooper()).post {
                            result.success(shouldRetry)
                        }
                    }
                }
                onCloseButton -> {
                    hostActivity?.finishActivity(1)
                    onCloseHandler()
                    result.success(null)
                }
                onDebug -> {
                    println("Debug log $args")
                    result.success(null)
                }
                else -> {
                    println("Method $method - Arguments $args")
                    result.success(null)
                }
            }
        }
    }

    /*
     Call this method to sign in sdk, it must be called after the app finishes signin and get profile
     + completion: error == nil -> success
    */
    fun signIn(profile: TixngoProfile, completion: (String?) -> Unit) {
        Handler(Looper.getMainLooper()).post {
            sdk.sendMessage(doSignIn, profile.toJson()) { result ->
                completion(result as? String)
            }
        }
    }

    /*
     Call this method to sign out sdk when user do signout in app
    */
    fun signOut() {
        Handler(Looper.getMainLooper()).post {
            sdk.sendMessage(doSignOut, null)
        }
    }

    /*
     Call this method to set profile to sdk when user profile is changed in app
    */
    fun setProfile(profile: TixngoProfile, completion: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            sdk.sendMessage(doSetProfile,  profile.toJson()) { _ ->
                completion()
            }
        }
    }

    /*
     Get authentication status of sdk, when call this method, sdk will ask jwt token from app. If jwt is nil, sdk will always sent false
    */
    fun getAuthStatus(completion: ((Boolean) -> Unit)) {
        Handler(Looper.getMainLooper()).post {
            sdk.sendMessage(getAuthStatus, null)  { result ->
                completion(result as Boolean)
            }
        }
    }

    /*
     Call this method to send push notification to sdk to process
    */
    fun processFcmMessageIfNeed(title: String?, body: String?, data: Map<String, String>?) {
        Handler(Looper.getMainLooper()).post {
            val notification = mutableMapOf<String, String>()
            if (title != null) {
                notification["title"] = title
            }
            if (body != null) {
                notification["body"] = body
            }
            val pushMessage = TixngoPushNotification(data, notification)
            sdk.sendMessage(processPushMessage, pushMessage.toJson())
        }
    }

    /*
     Call this method to send Firebase device token to sdk to process
    */
    fun processFcmTokenIfNeed(deviceToken: String) {
        Handler(Looper.getMainLooper()).post {
            sdk.sendMessage(processPushDeviceToken, deviceToken)
        }
    }

    /*
     Set environment of sdk
    */
    fun setEnv(env: TixngoEnv) {
        Handler(Looper.getMainLooper()).post {
            sdk.sendMessage(doSetEnv, env.value)
        }
    }

    fun openCurrentPage(activity: Activity) {
        val intent = sdk.rootIntent(activity)
        activity.startActivityForResult(intent, 1)
        hostActivity = activity
    }

    fun openHomePage(activity: Activity) {
        sdk.sendMessage(openHomePage, null)
        openCurrentPage(activity)
    }

    fun openEventPage(activity: Activity) {
        sdk.sendMessage(openEventPage, null)
        openCurrentPage(activity)
    }

    fun openTransferredTicketPage(activity: Activity) {
        sdk.sendMessage(openTransferredPage, null)
        openCurrentPage(activity)
    }

    fun openPendingTicketPage(activity: Activity) {
        sdk.sendMessage(openPendingPage, null)
        openCurrentPage(activity)
    }



}