package com.twiceyuan.permissionhandler


import android.app.Fragment
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

/**
 * Proxy Fragment
 */
class PermissionHolderFragment : Fragment() {

    companion object {
        private const val ARG_REQUEST_CODE = "requestCode"
        private const val ARG_PERMISSIONS = "permissions"

        fun newInstance(requestCode: Int, permissions: Array<out String>?) = PermissionHolderFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_REQUEST_CODE, requestCode)
                putStringArray(ARG_PERMISSIONS, permissions)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arguments.getStringArray(ARG_PERMISSIONS), arguments.getInt(ARG_REQUEST_CODE))
        } else {
            onResult(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == arguments.getInt(ARG_REQUEST_CODE)) {
            val requestCount = permissions?.size?.or(0)

            val grantedCount = grantResults
                    ?.filter { it == PackageManager.PERMISSION_GRANTED }
                    ?.count()?.or(0)

            if (grantedCount == requestCount) {
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    private fun onResult(isAllGranted: Boolean) {
        requestMap[arguments.getInt(ARG_REQUEST_CODE)]?.let {
            it(isAllGranted)
        }

        activity.fragmentManager.beginTransaction()
                .remove(this)
                .commit()
    }
}