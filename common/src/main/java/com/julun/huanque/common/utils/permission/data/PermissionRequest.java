package com.julun.huanque.common.utils.permission.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by nirack on 16-8-25.
 */

public class PermissionRequest implements Serializable{

    public final int requestCode;
    private ArrayList<String> permissions = new ArrayList<> ();

    private ArrayList<PermissionItemInfo> permissionInfo = new ArrayList<> ();

    private PermissionRequest (int code) {
        this.requestCode = code;
    }

    public static PermissionRequest request (int code) {
        return new PermissionRequest (code);
    }

    /**
     * 获取一个 {@link #PermissionRequest(int)} 对象,参数为需要请求的权限.
     * 这样的请求并不强制,也就是即使不赋予权限,也不应该影响接下来的操作.
     * 如果权限不通过则不能继续下面的操作,使用另外一个方法 {@link #requestMandatory(int, String...)} }
     *
     * @param permissions 需要请求的权限.
     * @return
     */
    public static PermissionRequest requestNotMandatory (int code,String... permissions) {
        final PermissionRequest request = request (code);
        if (permissions == null) {
            return request;
        }
        return request.addNotMandatoryRequests (false, permissions);
    }

    /**
     * 获取一个 {@link #PermissionRequest(int)} 对象,参数为需要请求的权限
     * 这样的请求并不强制,也就是即使不赋予权限,也不应该影响接下来的操作.
     * 如果不是,使用另外一个方法 {@link #request(int)}
     *
     *
     * @param code
     * @param permissions 需要请求的权限.
     * @return
     */
    public static PermissionRequest requestMandatory (int code, String... permissions) {
        final PermissionRequest request = request (code);
        if (permissions == null) {
            return request;
        }
        return request.addNotMandatoryRequests (true, permissions);
    }

    public PermissionRequest addNotMandatoryRequests (boolean mandatory, String... permissions) {
        for (String permission : permissions) {
            if (permission != null && permission.trim ().length () > 0) {
                add (PermissionItemInfo.optional (permission));
            }
        }
        return this;
    }

    public PermissionRequest addAll (Collection<PermissionItemInfo> callBack) {
        for (PermissionItemInfo itemInfo : callBack) {
            add (itemInfo);
        }
        return this;
    }


    public PermissionRequest add (PermissionItemInfo info) {
        permissionInfo.add (info);
        permissions.add (info.permission);
        return this;
    }


    public PermissionRequest addMandatory (String permission,String message) {
        return add (PermissionItemInfo.mandatory (permission).message (message));
    }

    public String[] getPermissionsRequest () {
        return permissions.toArray (new String[permissions.size ()]);
    }
}
