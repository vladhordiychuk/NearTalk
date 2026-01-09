package com.neartalk

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.neartalk.ui.theme.NearTalkTheme
import com.neartalk.navigation.AppNavigation
import com.neartalk.data.bluetooth.AndroidBluetoothController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.util.Log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var bluetoothController: AndroidBluetoothController

    private var showPermissionDialog by mutableStateOf(false)
    private var permissionsGranted by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        permissionsGranted = allGranted

        if (allGranted) {
            showPermissionDialog = false
            Log.d(TAG, "All permissions granted")
        } else {
            showPermissionDialog = true
            Log.w(TAG, "Some permissions denied: ${permissions.filter { !it.value }}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d(TAG, "onCreate called")
        requestBluetoothPermissions()

        setContent {
            NearTalkTheme {
                val navController = rememberNavController()

                AppNavigation(
                    navController = navController,
                    onMakeDiscoverable = { makeDeviceDiscoverable() }
                )

                if (showPermissionDialog) {
                    PermissionRationaleDialog(
                        onDismiss = { showPermissionDialog = false },
                        onGoToSettings = {
                            showPermissionDialog = false
                            openAppSettings()
                        }
                    )
                }
            }
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        Log.d(TAG, "Requesting permissions: ${permissions.joinToString()}")
        permissionLauncher.launch(permissions)
    }

    private fun makeDeviceDiscoverable() {
        if (!permissionsGranted) {
            Log.w(TAG, "Cannot make discoverable: permissions not granted")
            showPermissionDialog = true
            return
        }

        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }

        try {
            startActivity(discoverableIntent)
            Log.d(TAG, "Discoverable request sent")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when making discoverable: ${e.message}")
            showPermissionDialog = true
        } catch (e: Exception) {
            Log.e(TAG, "Exception when making discoverable: ${e.message}")
            showPermissionDialog = true
        }
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")

        // Check permissions after returning from Settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnect = checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasScan = checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            permissionsGranted = hasConnect && hasScan
        } else {
            val hasBluetooth = checkSelfPermission(android.Manifest.permission.BLUETOOTH) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            permissionsGranted = hasBluetooth
        }

        Log.d(TAG, "Permissions granted: $permissionsGranted")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called - isFinishing: $isFinishing")

        if (isFinishing) {
            try {
                bluetoothController.cleanup()
                Log.d(TAG, "Bluetooth cleanup completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error during cleanup: ${e.message}", e)
            }
        } else {
            // Activity is being recreated (rotation, etc.)
            Log.d(TAG, "Activity recreating - skipping cleanup")
        }

        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")

        try {
            bluetoothController.stopDiscovery()
            Log.d(TAG, "Discovery stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping discovery: ${e.message}")
        }
    }
}

@androidx.compose.runtime.Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Необхідні дозволи") },
        text = {
            Text(
                "Для роботи чату потрібен доступ до Bluetooth та локації (для пошуку пристроїв). " +
                        "Будь ласка, надайте ці дозволи в налаштуваннях.\n\n" +
                        "Необхідні дозволи:\n" +
                        "• Bluetooth (для з'єднання)\n" +
                        "• Локація (для пошуку пристроїв)\n" +
                        "• Bluetooth Scan/Connect (Android 12+)"
            )
        },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text("Налаштування")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрити")
            }
        }
    )
}