package com.atta.chatspherapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class InternetChecker {

    @SuppressLint("ObsoleteSdkInt")
    private suspend fun isInternetAvailable(context: Context): Boolean = withContext(Dispatchers.IO) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return@withContext capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return@withContext activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    private suspend fun hasInternetPackage(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val timeoutMs = 1500
            val socket = Socket()
            val socketAddress = InetSocketAddress("8.8.8.8", 53)

            socket.connect(socketAddress, timeoutMs)
            socket.close()

            return@withContext true
        } catch (e: IOException) {
            return@withContext false
        }
    }

    suspend fun isInternetConnectedWithPackage(context: Context): Boolean {
        return withContext(Dispatchers.IO) {isInternetAvailable(context) && hasInternetPackage(context)}
    }

}