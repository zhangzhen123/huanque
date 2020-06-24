package com.julun.huanque.common.utils.reflect;


import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 存储类信息的容器.
 * 需要更多功能的时候再添加.
 */
public class ClassInfo {
    /**
     * 类
     */
    private Class rawClass;

    private String name;

    private String simpleName;

    public static final Class<Object> objClass = Object.class;
    private boolean isTopClass;// 是否是顶级类,也就是 java.lang.Object

    /**
     * 当前类里声明的field.
     */
    private List<Field> declaredFields;

    /**
     * 所有的field,包括父级(如果有),但是刨除 java.lang.Object
     */
    private List<Field> allFields;

    private Map<String,Field> fieldMap;

    /**
     * 当前类里声明的 方法.
     */
    private List<Method> declaredMethods;

    /**
     * 所有的methods,包括父级(如果有),但是刨除 java.lang.Object
     */
    private List<Method> allMethods;

    private List<Constructor> declaredConstructors;

    public ClassInfo(@NonNull Class klass) {
        this.rawClass = klass;
        isTopClass = klass.equals(objClass);
        this.name = klass.getName();
        simpleName = klass.getSimpleName();
        if(!rawClass.isPrimitive() && !isMap()){
            fillAllFields();
            fillMethods();
            fillConstructors();
        }
    }

    public static ClassInfo get(@NonNull Class klass){
        return new ClassInfo (klass);
    }

    private void fillConstructors() {
        Constructor[] cons = rawClass.getDeclaredConstructors();
        this.declaredConstructors = new ArrayList<> ();
        for (Constructor con : cons) {
            declaredConstructors.add(con);
        }
    }

    private void fillMethods() {
        allMethods = new ArrayList<> ();
        declaredMethods = new ArrayList<> ();
        Method[] methods = rawClass.getDeclaredMethods();
        for (Method method : methods) {
            if (!method.getDeclaringClass().equals(objClass)) {
                allMethods.add(method);
                declaredMethods.add(method);
            }
        }
        if (isTopClass) {
            return;
        }
        allMethods.addAll(ReflectUtil.getClassInfo(rawClass.getSuperclass()).getAllMethods());
    }

    private void fillAllFields() {
        Field[] fields = this.rawClass.getDeclaredFields();
        declaredFields = new ArrayList<> ();
        allFields = new ArrayList<> ();
        fieldMap = new HashMap<> ();
        for (Field field : fields) {
            if (!field.getDeclaringClass().equals(objClass)) {
                declaredFields.add(field);
                allFields.add(field);
                fieldMap.put (field.getName (),field);
            }
        }
        if (isTopClass) {
            return;
        }
        Class superclass = rawClass.getSuperclass();
        if(null == superclass){
            throw new RuntimeException ("error: ==>> " + rawClass.getName() + "  \n \t " + superclass);
        }
        allFields.addAll(ReflectUtil.getClassInfo(superclass).getAllFields());
    }

    public List<Method> getDeclaredMethods() {
        return declaredMethods;
    }

    public void setDeclaredMethods(List<Method> declaredMethods) {
        this.declaredMethods = declaredMethods;
    }

    public Class getRawClass() {
        return rawClass;
    }

    public void setRawClass(Class rawClass) {
        this.rawClass = rawClass;
    }

    public List<Field> getDeclaredFields() {
        return declaredFields;
    }

    public void setDeclaredFields(List<Field> declaredFields) {
        this.declaredFields = declaredFields;
    }

    /**
     * 获取类的注解.
     *
     * @param anno
     * @param <T>
     * @return
     */
    public <T extends Annotation> T getAnnotation(@NonNull Class<? extends Annotation> anno) {
        Annotation annotation = rawClass.getAnnotation(anno);
        if (null == annotation) {
            return null;
        }
        return (T) annotation;
    }

    /**
     * 获取类的注解.
     *
     * @param <T[]>
     * @return
     */
    public <T extends Annotation> T[] getAnnotations() {
        Annotation[] annotations = rawClass.getAnnotations();
        if (null == annotations || annotations.length == 0) {
            return null;
        }
        return (T[]) annotations;
    }

    /**
     * 获取带有某个注解的方法.
     *
     * @param anno
     * @return
     */
    public List<Method> getMethodWithAnnotation(@NonNull Class<? extends Annotation> anno) {
        List<Method> result = new ArrayList<> ();
        for (Method method : this.declaredMethods) {
            if (method.isAnnotationPresent(anno)) {
                result.add(method);
            }
        }
        return result;
    }

    /**
     * 查找所有带 指定的注解的方法.
     *
     * @param anno
     * @return
     */
    public List<Field> getFieldsWithAnnotation(@NonNull Class<? extends Annotation> anno) {
        List<Field> fields = new ArrayList<> ();
        for (Field field : this.declaredFields) {
            if (field.isAnnotationPresent(anno)) {
                fields.add(field);
            }
        }
        return fields;
    }

    public List<Method> getMethodByName(String name) {
        List<Method> list = new ArrayList<> ();
        for (Method method : this.declaredMethods) {
            if (method.getName().equals(name)) {
                list.add(method);
            }
        }
        return list;
    }

    /**
     * 获取带有某个指定类型参数的 方法.
     *
     * @param name
     * @param contextClass
     * @return
     */
    public List<Method> getMethodByNameWithArgument(String name, Class<?> contextClass) {
        List<Method> list = new ArrayList<> ();
        for (Method method : this.declaredMethods) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (!method.getName().equals(name) || null == parameterTypes || parameterTypes.length == 0) {
                continue;
            }
            for (Class klass : parameterTypes) {
                if (contextClass.equals(klass)) {
                    list.add(method);
                    break;
                }
            }
        }
        return list;
    }

    /**
     * 获取构造函数. 只返回一个. 如果以后有更多需求,在做修改.
     *
     * @param argType
     * @return
     */
    public Constructor getConstructorByArgType(Class<?> argType) {
        boolean needArg = null != argType;
        for (Constructor con : this.declaredConstructors) {
            Class[] parameterTypes = con.getParameterTypes();
            if (!needArg && (parameterTypes == null || parameterTypes.length == 0)) {
                return con;
            } else if (needArg && parameterTypes != null && parameterTypes.length > 0) {
                for (Class kl : parameterTypes) {
                    if (kl.equals(argType)) {
                        return con;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 判断当前对象是否为一个类型。精确匹配，即使是父类和接口，也不相等
     *
     * @param type
     *            类型
     * @return 是否相等
     */
    public boolean is(Class<?> type) {
        return null != type && rawClass == type;
    }

    /**
     * 判断当前对象是否为一个类型。精确匹配，即使是父类和接口，也不相等
     *
     * @param className
     *            类型名称
     * @return 是否相等
     */
    public boolean is(String className) {
        return rawClass.getName().equals(className);
    }

    /**
     * @param type
     *            类型或接口名
     * @return 当前对象是否为一个类型的子类，或者一个接口的实现类
     */
    public boolean isOf(Class<?> type) {
        return type.isAssignableFrom(rawClass);
    }

    /**
     * @return 当前对象是否为字符串
     */
    public boolean isString() {
        return is(String.class);
    }

    /**
     * @return 当前对象是否为CharSequence的子类
     */
    public boolean isStringLike() {
        return CharSequence.class.isAssignableFrom(rawClass);
    }

    /**
     * @return 当前对象是否简单的数值，比如字符串，布尔，字符，数字，日期时间等
     */
    public boolean isSimple() {
        return isStringLike() || isBoolean() || isChar() || isNumber() || isDateTimeLike();
    }

    /**
     * @return 当前对象是否为字符
     */
    public boolean isChar() {
        return is(char.class) || is(Character.class);
    }

    /**
     * @return 当前对象是否为枚举
     */
    public boolean isEnum() {
        return rawClass.isEnum();
    }

    /**
     * @return 当前对象是否为布尔
     */
    public boolean isBoolean() {
        return is(boolean.class) || is(Boolean.class);
    }

    /**
     * @return 当前对象是否为浮点
     */
    public boolean isFloat() {
        return is(float.class) || is(Float.class);
    }

    /**
     * @return 当前对象是否为双精度浮点
     */
    public boolean isDouble() {
        return is(double.class) || is(Double.class);
    }

    /**
     * @return 当前对象是否为整型
     */
    public boolean isInt() {
        return is(int.class) || is(Integer.class);
    }

    /**
     * @return 当前对象是否为整数（包括 int, long, short, byte）
     */
    public boolean isIntLike() {
        return isInt() || isLong() || isShort() || isByte() || is(BigDecimal.class);
    }

    /**
     * @return 当前类型是不是接口
     */
    public boolean isInterface() {
        return rawClass.isInterface();
    }

    /**
     * @return 当前对象是否为小数 (float, dobule)
     */
    public boolean isDecimal() {
        return isFloat() || isDouble();
    }

    /**
     * @return 当前对象是否为长整型
     */
    public boolean isLong() {
        return is(long.class) || is(Long.class);
    }

    /**
     * @return 当前对象是否为短整型
     */
    public boolean isShort() {
        return is(short.class) || is(Short.class);
    }

    /**
     * @return 当前对象是否为字节型
     */
    public boolean isByte() {
        return is(byte.class) || is(Byte.class);
    }

    /**
     * @param type
     *            类型
     * @return 否为一个对象的外覆类
     */
    public boolean isWrapperOf(Class<?> type) {
        try {
            return ReflectUtil.getClassInfo (type).getWrapperClass() == rawClass;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * @param type
     *            目标类型
     * @return 判断当前对象是否能直接转换到目标类型，而不产生异常
     */
    public boolean canCastToDirectly(Class<?> type) {
        if (rawClass == type || type.isAssignableFrom(rawClass))
            return true;
        if (rawClass.isPrimitive() && type.isPrimitive()) {
            if (this.isPrimitiveNumber() && ReflectUtil.getClassInfo (type).isPrimitiveNumber())
                return true;
        }
        try {
            return ReflectUtil.getClassInfo (type).getWrapperClass() == this.getWrapperClass();
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * @return 获得外覆类
     *
     * @throws RuntimeException
     *             如果当前类型不是原生类型，则抛出
     */
    public Class<?> getWrapperClass() {
        if (!rawClass.isPrimitive()) {
            if (this.isPrimitiveNumber() || this.is(Boolean.class) || this.is(Character.class)) {
                return rawClass;
            }
            throw new IllegalArgumentException (
                    String.format("Class '%s' should be a primitive class!", rawClass.getName()));
        }
        // TODO 用散列能快一点
        if (is(int.class)) {
            return Integer.class;
        }
        if (is(char.class)) {
            return Character.class;
        }
        if (is(boolean.class)) {
            return Boolean.class;
        }
        if (is(long.class)) {
            return Long.class;
        }
        if (is(float.class)) {
            return Float.class;
        }
        if (is(byte.class)) {
            return Byte.class;
        }
        if (is(short.class)) {
            return Short.class;
        }
        if (is(double.class)) {
            return Double.class;
        }
        throw new IllegalArgumentException (String.format("Class [%s] has no wrapper class!", rawClass.getName()));
    }

    /**
     * @return 当前对象是否为原生的数字类型 （即不包括 boolean 和 char）
     */
    public boolean isPrimitiveNumber() {
        return isInt() || isLong() || isFloat() || isDouble() || isByte() || isShort();
    }

    /**
     * 如果不是容器，也不是 POJO，那么它必然是个 Obj
     *
     * @return true or false
     */
    public boolean isObj() {
        return isContainer() || isPojo();
    }

    /**
     * 判断当前类型是否为POJO。 除了下面的类型，其他均为 POJO
     * <ul>
     * <li>原生以及所有包裹类
     * <li>类字符串
     * <li>类日期
     * <li>非容器
     * </ul>
     *
     * @return true or false
     */
    public boolean isPojo() {
        if (this.rawClass.isPrimitive() || this.isEnum())
            return false;

        if (this.isStringLike() || this.isDateTimeLike())
            return false;

        if (this.isPrimitiveNumber() || this.isBoolean() || this.isChar())
            return false;

        return !isContainer();
    }

    /**
     * 判断当前类型是否为容器，包括 Map，Collection, 以及数组
     *
     * @return true of false
     */
    public boolean isContainer() {
        return isColl() || isMap();
    }

    /**
     * 判断当前类型是否为数组
     *
     * @return true of false
     */
    public boolean isArray() {
        return rawClass.isArray();
    }

    /**
     * 判断当前类型是否为 Collection
     *
     * @return true of false
     */
    public boolean isCollection() {
        return isOf(Collection.class);
    }

    /**
     * @return 当前类型是否是数组或者集合
     */
    public boolean isColl() {
        return isArray() || isCollection();
    }

    /**
     * 判断当前类型是否为 Map
     *
     * @return true of false
     */
    public boolean isMap() {
        return isOf(Map.class);
    }

    /**
     * @return 当前对象是否为数字
     */
    public boolean isNumber() {
        return Number.class.isAssignableFrom(rawClass)
                || rawClass.isPrimitive() && !is(boolean.class) && !is(char.class);
    }

    /**
     * @return 当前对象是否在表示日期或时间
     */
    public boolean isDateTimeLike() {
        return isCalendar() || java.util.Date.class.isAssignableFrom(rawClass)
                || java.sql.Date.class.isAssignableFrom(rawClass) || java.sql.Time.class.isAssignableFrom(rawClass);
    }

    public boolean isCalendar() {
        return Calendar.class.isAssignableFrom(rawClass);
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getName() {
        return name;
    }

    public List<Field> getAllFields() {
        return allFields;
    }

    public List<Method> getAllMethods() {
        return allMethods;
    }

    public Map<String, Field> getFieldMap () {
        return fieldMap;
    }

}
