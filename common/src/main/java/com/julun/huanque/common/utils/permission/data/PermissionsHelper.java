package com.julun.huanque.common.utils.permission.data;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import androidx.core.content.PermissionChecker;


import com.julun.huanque.common.init.CommonInit;

import java.util.ArrayList;
import java.util.List;

/**
 * 检查权限的工具类
 * <p/>
 */
public class PermissionsHelper {

    /**
     * 每次请求之后回调的函数,请求结束之后自动置空.
     */
    private static PermissionRequestCallback callback;

    //一次肯定只处理一个请求,值保留一个变量就欧了
    private static PermissionRequest currentRequest;

    /**
     * 是否缺少某些权限
     * @param permissions
     * @return
     */
    public static boolean isLackOfPermission (String... permissions) {
        for (String permission : permissions) {
            boolean permissionDenied = isNotGranted (permission);
            if (permissionDenied) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    @SuppressLint("WrongConstant")
    private static boolean isNotGranted (String permission) {
        return PermissionChecker.checkSelfPermission (CommonInit.Companion.getInstance().getApp(), permission)
                == PackageManager.PERMISSION_DENIED;
    }


    /**
     * 请求权限的回调.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public static void result (int requestCode, final String[] permissions, int[] grantResults) {
        PermissionResponse response = wrapResponse(permissions,grantResults);
        if (currentRequest == null) {
            return;
        }
        if (currentRequest.requestCode!=requestCode) {
            return;
        }
        System.out.println (response);
    }

    private static PermissionResponse wrapResponse (String[] permissions, final int[] grantResults) {
        List<String> list = new ArrayList<> ();
        for (int index = 0; index < permissions.length; index++) {
            if (PackageManager.PERMISSION_GRANTED != grantResults[index]) {
                list.add (permissions[index]);
            }
        }
        return list.size () ==0 ? PermissionResponse.allGranted ():PermissionResponse.notAllGranted (list.toArray (new String[list.size ()]));
    }

    public static void setCallback (PermissionRequestCallback callback) {
        PermissionsHelper.callback = callback;
    }

    public static PermissionResponse request (PermissionRequest request) {
        PermissionResponse response = PermissionResponse.allGranted ();
        return response;
    }
}
