package com.julun.huanque.common.utils.permission.data;

/**
 * Created by nirack on 16-8-24.
 */

public abstract class PermissionRequestCallback {
    /**
     * 回调完成.
     *
     * @param allGranted 是否请求的权限都通过了.
     * @param rejected   被拒绝的权限.
     */
    public abstract void requestResult (boolean allGranted, String[] rejected);

}
