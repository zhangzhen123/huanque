package com.julun.huanque.common.utils.reflect;



import com.julun.huanque.common.basic.Root;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ReflectUtil {

    private static final HashMap<Class,Class> PRIMARY_CLASS = new HashMap<>();
    static {
        PRIMARY_CLASS.put(long.class ,Long.class);
        PRIMARY_CLASS.put(float.class ,Float.class);
        PRIMARY_CLASS.put(int.class ,Integer.class);
        PRIMARY_CLASS.put(byte.class ,Byte.class);
        PRIMARY_CLASS.put(short.class ,Short.class);
        PRIMARY_CLASS.put(boolean.class ,Boolean.class);
        PRIMARY_CLASS.put(char.class ,Character.class);
        PRIMARY_CLASS.put(double.class ,Double.class);
        PRIMARY_CLASS.put(void.class ,Void.class);
    }

    public static Class getWrappedClass(Class clazz){
        if(!clazz.isPrimitive()){
            throw new IllegalArgumentException("参数必须是基本类型");
        }
        final Class primaryClass = PRIMARY_CLASS.get(clazz);
        return primaryClass;
    }


    private ReflectUtil () {
        throw new UnsupportedOperationException ("不要实例化这个了....");
    }

    private static final Hashtable<String, ParameterizedType> typeMap = new Hashtable<> ();

    private static final Hashtable<String, ClassInfo> classStoreMap = new Hashtable<> ();

    public static ParameterizedType type(final Class<?> raw, final Type... args) {
        String key = getTypeKey(raw, args);
        ParameterizedType type = typeMap.get(key);
        if (type == null) {
            type = new ParameterizedTypeSimpleImpl (raw, args);
            typeMap.put(key, type);
        }
        return type;
    }

    public static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException ("Missing type parameter.");
        }
        ParameterizedType parameterizedType = (ParameterizedType) superclass;
        Type actualType = parameterizedType.getActualTypeArguments()[0];
        return actualType;
    }

    private static String getTypeKey(Class<?> raw, Type... args) {
        String result = raw.getName();
        if (args != null) {
            for (Type type : args) {
                result += ":" + type.toString();
            }
        }
        return result;
    }

    /**
     * 生成List<Bean>形式的泛型信息
     *
     * @param clazz
     *            泛型的具体类
     * @return List<clazz>形式的泛型Type
     */
    public static Type list(Type clazz) {
        return type(List.class, clazz);
    }

    /**
     * 获取分页 PageResult<Bean> 的泛型 信息.
     *
     * @param clazz
     * @return
     */
    public static Type root(Type clazz) {
        return type(Root.class, clazz);
    }

    /**
     * 生成Bean[]形式的泛型信息
     */
    public static Type array(Class<?> clazz) {
        return type(Array.newInstance(clazz, 0).getClass());
    }

    /**
     * 生成Map<key,serviceClass>形式的泛型Type
     *
     * @param key
     *            key的泛型
     * @param value
     *            value的泛型
     * @return Map<key,serviceClass>形式的泛型Type
     */
    public static Type map(Type key, Type value) {
        return type(Map.class, key, value);
    }

    /**
     * 生成Map<String,serviceClass>形式的泛型Type
     *
     * @param value
     *            value的泛型
     * @return Map<String,serviceClass>形式的泛型Type
     */
    public static Type mapStr(Type value) {
        return map(String.class, value);
    }

    public static ClassInfo getClassInfo(Class<?> klass) {
        String key = klass.getName();
        ClassInfo classInfo = classStoreMap.get(key);
        if (null == classInfo) {
            classInfo = new ClassInfo(klass);
            classStoreMap.put(key, classInfo);
        }
        return classInfo;
    }

    public static ClassInfo getClassInfo(String key) {
        ClassInfo classInfo = classStoreMap.get(key);
        if (null == classInfo) {
            try {
                classInfo = new ClassInfo(Class.forName (key));
            } catch (ClassNotFoundException e) {
                e.printStackTrace ();
                throw new RuntimeException ("找不到需要的类 ==>> " + key ,e);
            }
            classStoreMap.put(key, classInfo);
        }
        return classInfo;
    }

    public static <T> ClassInfo getClassInfo(T bean) {
        return getClassInfo(bean.getClass());
    }

    public static Class<?> getClass(String initializerClass) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(initializerClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return aClass;
    }
}
