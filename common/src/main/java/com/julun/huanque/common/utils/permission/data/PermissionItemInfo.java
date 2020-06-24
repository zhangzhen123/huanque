package com.julun.huanque.common.utils.permission.data;

import java.io.Serializable;

/**
 * 请求权限时候的信息需要的对象.
 */
public class PermissionItemInfo implements Serializable{
    public boolean mandatory = false;
    public String permission;//请求的权限
    public String message;//被拒绝的时候的提示

    public PermissionItemInfo (String permission) {
        this.permission = permission;
    }

    public static PermissionItemInfo mandatory (String permission) {
        return optional (permission).mandatory (true);
    }

    /**
     * 非强制性权限,没有也能继续运行程序.
     * @param permission
     * @return
     */
    public static PermissionItemInfo optional (String permission) {
        return new PermissionItemInfo (permission);
    }

    /**
     * 设置消息,在不被赋权的时候,提示消息
     * @param message
     * @return
     */
    public PermissionItemInfo message (String message) {
        this.message = message;
        return this;
    }

    private PermissionItemInfo mandatory (boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }
}