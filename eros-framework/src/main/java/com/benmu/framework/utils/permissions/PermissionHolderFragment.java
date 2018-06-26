package com.benmu.framework.utils.permissions;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class PermissionHolderFragment extends Fragment {

    private static final String ARG_REQUEST_CODE = "requestCode";
    private static final String ARG_PERMISSIONS  = "permissions";

    public static final PermissionHolderFragment newInstance(int requestCode, @Nullable String[] permissions) {
        PermissionHolderFragment var3 = new PermissionHolderFragment();
        Bundle var5 = new Bundle();
        var5.putInt("requestCode", requestCode);
        var5.putStringArray("permissions", permissions);
        var3.setArguments(var5);
        return var3;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= 23) {
            this.requestPermissions(this.getArguments().getStringArray("permissions"), this.getArguments().getInt("requestCode"));
        } else {
            this.onResult(true);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @Nullable int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == this.getArguments().getInt("requestCode")) {
            Integer requestCount = permissions != null ? permissions.length | 0 : null;
            Integer var10000;
            if (grantResults == null) {
                var10000 = null;
            } else {
                int[] $receiver$iv$iv = grantResults;
                Collection destination$iv$iv = (Collection) (new ArrayList());
                int var9 = grantResults.length;

                for (int var10 = 0; var10 < var9; ++var10) {
                    int element$iv$iv = $receiver$iv$iv[var10];
                    if (element$iv$iv == 0) {
                        destination$iv$iv.add(element$iv$iv);
                    }
                }

                Collection var6 = (Collection) ((List) destination$iv$iv);
                var10000 = var6.size() | 0;
            }

            Integer grantedCount = var10000;
            if (grantedCount.equals(requestCount)) {
                this.onResult(true);
            } else {
                this.onResult(false);
            }
        }

    }

    private final void onResult(boolean isAllGranted) {
        Function1<Boolean> var10000 = (Function1<Boolean>) PermissionsKt.getRequestMap().get(this.getArguments().getInt("requestCode"));
        if (var10000 != null) {
            Function1 var2 = var10000;
            var2.invoke(isAllGranted);
        }

        Activity var5 = this.getActivity();
        var5.getFragmentManager().beginTransaction().remove((Fragment) this).commit();
    }
}
