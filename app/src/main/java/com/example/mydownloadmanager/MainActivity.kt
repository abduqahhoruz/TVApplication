package com.example.mydownloadmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.mydownloadmanager.common.Constants
import com.example.mydownloadmanager.common.MyKeyEventListener
import com.example.mydownloadmanager.ui.home.notification.NotificationItem
import com.example.mydownloadmanager.ui.home.notification.NotificationUtils
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper


private const val REQ_CODE_DM = 1001

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    val notificationHelper: FileDownloadNotificationHelper<NotificationItem> =
        FileDownloadNotificationHelper<NotificationItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpNavigation()
        checkUpPermission()
        FileDownloader.setupOnApplicationOnCreate(application)
    }

    private fun setUpNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupFileDownloader() {
        FileDownloader.setupOnApplicationOnCreate(application)
            .connectionCreator(
                FileDownloadUrlConnection.Creator(
                    FileDownloadUrlConnection.Configuration()
                        .connectTimeout(15000) // set connection timeout.
                        .readTimeout(15000) // set read timeout.
                )
            )
            .commit()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currentFragment = supportFragmentManager.fragments.first().childFragmentManager.fragments.first()
        if (currentFragment != null && currentFragment is MyKeyEventListener) {
            (currentFragment as MyKeyEventListener).onKeyDown(keyCode, event)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun checkUpPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQ_CODE_DM
                )
            }
        } else {
            /***/
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQ_CODE_DM -> {
                if (grantResults[0] != PackageManager.PERMISSION_DENIED) {
                    checkUpPermission()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationHelper.clear()
        NotificationUtils.deleteNotificationChannel(Constants.CHANNEL_ID, applicationContext)
    }
}