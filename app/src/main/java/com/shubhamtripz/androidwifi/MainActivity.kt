package com.shubhamtripz.androidwifi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiListView: ListView
    private lateinit var wifiScanResults: List<ScanResult>


    companion object {
        const val WIFI_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiListView = findViewById(R.id.wifi_list_view)

        // Get the WifiManager system service
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Set up the list view adapter
        wifiListView.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())

        // Set up the list view click listener
        wifiListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val itemValue = parent.getItemAtPosition(position) as String
                val scanResult = wifiScanResults.find { it.SSID == itemValue }
                if (scanResult != null) {
                    Toast.makeText(
                        applicationContext,
                        "SSID: ${scanResult.SSID}\nBSSID: ${scanResult.BSSID}\nSignal Strength: ${scanResult.level} dBm",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        // Check if we have Wi-Fi permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CHANGE_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request Wi-Fi permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                WIFI_PERMISSION_CODE
            )
        }
    }


    // Button click handler for toggling Wi-Fi on/off
    fun toggleWifi(view: View) {
        if (wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = false
            Toast.makeText(applicationContext, "Wi-Fi disabled", Toast.LENGTH_SHORT).show()
        } else {
            wifiManager.isWifiEnabled = true
            Toast.makeText(applicationContext, "Wi-Fi enabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == WIFI_PERMISSION_CODE) {
            // Check if the permission was granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Wi-Fi permissions granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(applicationContext, "Wi-Fi permissions denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Button click handler for scanning for nearby Wi-Fi networks
    fun scanWifi(view: View) {
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(applicationContext, "Please enable Wi-Fi first", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Start the Wi-Fi scan
        wifiManager.startScan()

        // Get the scan results
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                WIFI_PERMISSION_CODE)
        }
        wifiScanResults = wifiManager.scanResults

        // Update the list view adapter with the new scan results
        val adapter = wifiListView.adapter as ArrayAdapter<String>
        adapter.clear()
        for (scanResult in wifiScanResults) {
            adapter.add(scanResult.SSID)
        }
        adapter.notifyDataSetChanged()
    }
}

