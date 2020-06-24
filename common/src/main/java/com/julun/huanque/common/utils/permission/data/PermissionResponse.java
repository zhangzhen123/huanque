package com.julun.huanque.common.utils.permission.data;

import androidx.annotation.NonNull;

/**
 * 权限请求的返回结果.
 * Created by nirack on 16-8-25.
 */
public class PermissionResponse {
    public final boolean allGranted;
    public String [] notGranted;

    private PermissionResponse (boolean allGranted) {
        this.allGranted = allGranted;
    }

    /**
     * 所有请求的权限都获取了.
     * @return
     */
    public static PermissionResponse allGranted(){
        PermissionResponse response = new PermissionResponse (true);
        return response;
    }


    /**
     * 没有全部获取所有请求的权限
     * @param permissions  未被授权的权限.
     * @return
     */
    public static PermissionResponse notAllGranted(@NonNull String ... permissions){
        PermissionResponse response = new PermissionResponse (false);
        response.notGranted = permissions;
        return response;
    }

}
