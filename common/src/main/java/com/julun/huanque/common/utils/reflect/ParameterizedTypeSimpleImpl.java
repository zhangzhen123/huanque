package com.julun.huanque.common.utils.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 通过实现ParameterizedType,达到无需通过反射得到泛型Type</p> 通过嵌套Map/List,可得出无限可能
 */
public class ParameterizedTypeSimpleImpl implements ParameterizedType {

    private ParameterizedTypeSimpleImpl() {}

    public ParameterizedTypeSimpleImpl(Type rawType, Type... actualTypeArguments) {
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
    }

    private Type[] actualTypeArguments;

    private Type rawType;

    private Type ownerType;

    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    public Type getRawType() {
        return rawType;
    }

    public Type getOwnerType() {
        return ownerType;
    }

    public void setActualTypeArguments(Type... actualTypeArguments) {
        this.actualTypeArguments = actualTypeArguments;
    }

    public void setOwnerType(Type ownerType) {
        this.ownerType = ownerType;
    }

    public void setRawType(Type rawType) {
        this.rawType = rawType;
    }
}
