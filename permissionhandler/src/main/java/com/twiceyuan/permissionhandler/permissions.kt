package com.twiceyuan.permissionhandler

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by twiceYuan on 2017/11/22.
 *
 * 动态权限封装
 */
val permissionRequestCode = AtomicInteger(1)

const val permissionRequestTag = "permissionRequestTag"

val requestMap = hashMapOf<Int, (isGranted: Boolean) -> Unit>()

fun Activity.requestPermissionsWithCallback(
        vararg permissions: String,
        callback: (isGranted: Boolean) -> Unit
) {
    // 判断权限是否已经被允许，如果全部已经被允许则不进行请求（如果不进行该判断，副作用是会额外触发 Activity 的 onPause 回调）
    var isAllGranted = true

    permissions.forEach {
        if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
            isAllGranted = false
            return@forEach
        }
    }

    if (isAllGranted) {
        callback(true)
        return
    }

    requestPermissionsWithCallback(permissionRequestCode.getAndIncrement(), permissions, callback)
}

fun Activity.requestPermissionsWithCallback(
        requestCode: Int,
        permissions: Array<out String>,
        callback: (isGranted: Boolean) -> Unit
) {
    requestMap[requestCode] = callback
    val fragment = PermissionHolderFragment.newInstance(requestCode, permissions)

    fragmentManager.beginTransaction()
            .add(fragment, permissionRequestTag)
            .commit()
}

