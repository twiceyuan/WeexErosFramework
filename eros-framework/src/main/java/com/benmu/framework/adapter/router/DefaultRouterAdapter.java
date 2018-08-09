package com.benmu.framework.adapter.router;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.benmu.framework.BMWXEnvironment;
import com.benmu.framework.R;
import com.benmu.framework.activity.AbstractWeexActivity;
import com.benmu.framework.constant.Constant;
import com.benmu.framework.manager.ManagerFactory;
import com.benmu.framework.manager.impl.ModalManager;
import com.benmu.framework.manager.impl.ParseManager;
import com.benmu.framework.model.CallPhoneBean;
import com.benmu.framework.model.RouterModel;
import com.benmu.framework.model.WebViewParamBean;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.bridge.SimpleJSCallback;
import com.twiceyuan.permissionx.PermissionX;
import com.twiceyuan.permissionx.functions.PermissionOnDenied;
import com.twiceyuan.permissionx.functions.PermissionOnGranted;

import java.util.Map;


/**
 * Created by Carry on 2017/8/21.
 */

public class DefaultRouterAdapter {
    private static DefaultRouterAdapter mInstance = new DefaultRouterAdapter();
    private String mApplicationId;

    private DefaultRouterAdapter() {
    }

    public static DefaultRouterAdapter getInstance() {
        return mInstance;
    }

    public static boolean refresh(Context context) {
        if (context instanceof AbstractWeexActivity) {
            ((AbstractWeexActivity) context).refresh();
            return true;
        }
        return false;
    }

    public static boolean finish(Context context) {
        if (context instanceof AbstractWeexActivity) {
            ((AbstractWeexActivity) context).finish();
            return true;
        }
        return false;
    }

    public String getPageCategory(Context context) {
        if (TextUtils.isEmpty(mApplicationId)) {
            mApplicationId = context.getApplicationInfo().packageName;
        }
        return mApplicationId + ".categoty.page";
    }

    public String getWebViewCategory(Context context) {
        if (TextUtils.isEmpty(mApplicationId)) {
            mApplicationId = context.getApplicationInfo().packageName;
        }
        return mApplicationId + ".category.web";
    }

    public boolean open(Context context, String params, JSCallback jsCallback) {
        ParseManager parseManager = ManagerFactory.getManagerService(ParseManager.class);
        RouterModel routerModel = parseManager.parseObject(params, RouterModel.class);
        if (jsCallback != null) {
            routerModel.backCallback = (SimpleJSCallback) jsCallback;
        }
        return !(routerModel == null || !(context instanceof Activity)) && performStartActivity(
                (Activity) context, routerModel, getPageCategory(context));
    }

    private boolean performStartActivity(Activity activity, RouterModel routerModel, String
            bmpageCategory) {
        String pathUrl = routerModel.url;
        if (TextUtils.isEmpty(pathUrl)) return false;
        Uri pathUri = Uri.parse(pathUrl);
        if (!TextUtils.equals("http", pathUri.getScheme()) && !TextUtils.equals("https", pathUri
                .getScheme())) {
            pathUri = Uri.parse(BMWXEnvironment.mPlatformConfig.getUrl().getJsServer() +
                    "/dist/js" + pathUrl);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Constant.ROUTERPARAMS, routerModel);
        intent.setData(pathUri);
        intent.addCategory(bmpageCategory);
        activity.startActivity(intent);

        if (Constant.ACTIVITIES_ANIMATION.ANIMATION_PUSH.equals(routerModel.type)) {
            activity.overridePendingTransition(R.anim.right_in, R.anim.view_stay);
        } else if (Constant.ACTIVITIES_ANIMATION.ANIMATION_PRESENT.equals(routerModel
                .type)) {
            activity.overridePendingTransition(R.anim.bottom_in, R.anim.view_stay);
        } else if (Constant.ACTIVITIES_ANIMATION.ANIMATION_TRANSLATION.equals(routerModel
                .type)) {
            activity.overridePendingTransition(R.anim.left_in, R.anim.view_stay);
        } else {
            activity.overridePendingTransition(R.anim.right_in, R.anim.view_stay);
        }
        return true;
    }

    public boolean back(Context context, String params) {
        ParseManager parseManager = ManagerFactory.getManagerService(ParseManager.class);
        RouterModel routerModel = parseManager.parseObject(params, RouterModel.class);
        int backLength = routerModel.length;
        if (backLength >= RouterTracker.length()) {
            RouterTracker.clearActivitySurplus(1);
        } else {
            for (int i = 0; i < backLength; i++) {
                RouterTracker.popActivity();
            }
        }

        Object backParams = routerModel.params;
        if (backParams instanceof Map) {
            if (((Map) backParams).size() > 0) {
                Activity activity = RouterTracker.peekActivity();
                if (activity instanceof AbstractWeexActivity) {
                    //设置back参数
                    ((AbstractWeexActivity) activity).setRouterParam(routerModel);
                }
            }
        }

        return true;
    }

    public RouterModel getParams(Context context) {
        if (context == null) return null;
        Activity activity = RouterTracker.peekActivity();
        if (activity instanceof AbstractWeexActivity) {
            return ((AbstractWeexActivity) activity).getRouterParam();
        }
        return null;
    }

    public void dialing(final Context context, String params) {
        ParseManager parseManager = ManagerFactory.getManagerService(ParseManager.class);
        CallPhoneBean callPhone = null;
        callPhone = parseManager.parseObject(params, CallPhoneBean.class);
        if (callPhone == null) {
            callPhone = new CallPhoneBean();
            callPhone.to = "110";
        }
        if (TextUtils.isEmpty(callPhone.to) || context == null) return;

        final String finalPhone = callPhone.to;

        if (!callPhone.tip) {
            callPhone(String.valueOf(finalPhone), context);
        } else {
            ModalManager.BmAlert.showAlert(context, null, String.valueOf(finalPhone), "呼叫", new
                    DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            callPhone(String.valueOf(finalPhone), context);
                            dialog.dismiss();
                        }
                    }, "取消", null, null, null);
        }
    }

    private void callPhone(final String finalPhone, final Context context) {
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;

            PermissionX.request(activity, new String[]{
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
            }).onGranted(new PermissionOnGranted() {
                @SuppressLint("MissingPermission")
                @Override
                public void onAllGranted() {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + finalPhone));
                    activity.startActivity(intent);
                }
            }).onDenied(new PermissionOnDenied() {
                @Override
                public void onAnyDenied() {
                    Toast.makeText(context, "权限被拒绝", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void toWebView(Context context, String params) {
        if (TextUtils.isEmpty(params)) return;
        ParseManager parseManager = ManagerFactory.getManagerService(ParseManager.class);
        WebViewParamBean webViewParamBean = parseManager.parseObject(params, WebViewParamBean
                .class);
        String title = webViewParamBean.getTitle();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(getWebViewCategory(context));
        String type = webViewParamBean.getType() == null ? Constant.ACTIVITIES_ANIMATION
                .ANIMATION_PUSH : webViewParamBean.getType();
        RouterModel routerModel = new RouterModel(null, type, null, title, webViewParamBean
                .isNavShow(),
                null);
        intent.putExtra(Constant.ROUTERPARAMS, routerModel);
        intent.putExtra(Constant.WEBVIEW_PARAMS, webViewParamBean);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.startActivity(intent);
            if (Constant.ACTIVITIES_ANIMATION.ANIMATION_PUSH.equals(routerModel.type)) {
                activity.overridePendingTransition(R.anim.right_in, R.anim.view_stay);
            } else if (Constant.ACTIVITIES_ANIMATION.ANIMATION_PRESENT.equals(routerModel
                    .type)) {
                activity.overridePendingTransition(R.anim.bottom_in, R.anim.view_stay);
            } else if (Constant.ACTIVITIES_ANIMATION.ANIMATION_TRANSLATION.equals(routerModel
                    .type)) {
                activity.overridePendingTransition(R.anim.left_in, R.anim.view_stay);
            } else {
                activity.overridePendingTransition(R.anim.right_in, R.anim.view_stay);
            }
        }

    }

    public boolean openBrowser(Context context, String params) {
        if (context == null || TextUtils.isEmpty(params)) return false;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri parse = Uri.parse(params);
        intent.setData(parse);
        context.startActivity(intent);
        return true;
    }

    public static class Model {
        public String  to;
        public boolean tip;
    }
}
