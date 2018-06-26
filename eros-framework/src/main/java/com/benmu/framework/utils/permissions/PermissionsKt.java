package com.benmu.framework.utils.permissions;


import android.app.Activity;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PermissionsKt {

    public static final String permissionRequestTag = "permissionRequestTag";

    private static final AtomicInteger permissionRequestCode = new AtomicInteger(1);

    private static final HashMap<Integer, Function1<Boolean>> requestMap = new HashMap<>();


    public static final AtomicInteger getPermissionRequestCode() {
        return permissionRequestCode;
    }


    public static final HashMap getRequestMap() {
        return requestMap;
    }

    public static final void requestPermissionsWithCallback(Activity $receiver, String[] permissions, Function1<Boolean> callback) {
        boolean isAllGranted = true;
        String[] $receiver$iv = permissions;
        int var5 = permissions.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            String element$iv = $receiver$iv[var6];
            if (ContextCompat.checkSelfPermission($receiver, element$iv) != 0) {
                isAllGranted = false;
            }
        }

        if (isAllGranted) {
            callback.invoke(true);
        } else {
            requestPermissionsWithCallback($receiver, permissionRequestCode.getAndIncrement(), permissions, callback);
        }
    }

    public static final void requestPermissionsWithCallback(Activity $receiver, int requestCode, String[] permissions, Function1 callback) {
        Map var4 = (Map) requestMap;
        Integer var5 = requestCode;
        var4.put(var5, callback);
        PermissionHolderFragment fragment = PermissionHolderFragment.newInstance(requestCode, permissions);
        $receiver.getFragmentManager().beginTransaction().add((Fragment) fragment, "permissionRequestTag").commit();
    }
}
