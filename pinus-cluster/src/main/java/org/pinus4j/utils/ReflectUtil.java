package org.pinus4j.utils;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectUtil {

    private static final Map<Class<?>, Field[]> fieldCache = new HashMap<Class<?>, Field[]>();

    public static Object newObject(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        Object instance = null;

        try {
            instance = clazz.newInstance();
        } catch (InstantiationException e) {
            Constructor<?>[] constructors = clazz.getConstructors();
            for (Constructor<?> c : constructors) {
                Class<?>[] paramClasses = c.getParameterTypes();
                Object[] paramObj = new Object[paramClasses.length];
                for (int i = 0; i < paramClasses.length; i++) {
                    paramObj[i] = newObject(paramClasses[i]);
                }
                try {
                    instance = c.newInstance(paramObj);
                } catch (Exception e1) {
                }

                if (instance != null) {
                    break;
                }
            }
        }

        if (instance == null) {
            throw new InstantiationException();
        }

        return instance;
    }

    public static Field[] getFieldsWithoutCache(Class<?> clazz, String prefix) {
        List<Field> fields = new ArrayList<Field>();

        for (Field f : clazz.getFields()) {
            if (f.getName().startsWith(prefix)) {
                fields.add(f);
            }
        }

        return fields.toArray(new Field[fields.size()]);
    }

    /**
     * 获取一个对象的所有定义字段. 从父类开始算起，第一个被标志为壳序列化的类的字段及其子类的字段的可继承字段 当前类的所有字段.
     *
     * @param clazz 对象的class.
     * @return 定义字段.
     */
    public static Field[] getFields(Class<?> clazz) {
        Field[] fields = fieldCache.get(clazz);

        if (fields == null) {
            // 获取所有的父类，判断从哪个父类开始有序列化能力.
            List<Class<?>> c1 = new ArrayList<Class<?>>();
            Class<?> curClass = clazz;
            while (curClass != Object.class) {
                c1.add(curClass);
                curClass = curClass.getSuperclass();
            }
            int index = 0;
            for (int i = c1.size() - 1; i >= 0; i--) {
                Class<?> checkClass = c1.get(i);
                for (Class<?> interf : checkClass.getInterfaces()) {
                    if (interf == Serializable.class) {
                        index = i;
                        break;
                    }
                }
            }
            // 存放有序列化能力的对象
            List<Class<?>> c2 = new ArrayList<Class<?>>();
            c2.addAll(c1.subList(0, index + 1));

            // 获取当前类的私有属性, 不要父类的私有属性
            List<Field> fieldList = new ArrayList<Field>();
            boolean curPrivate = true;
            for (Class<?> c3 : c2) {
                for (Field f : c3.getDeclaredFields()) {
                    if (Modifier.isFinal(f.getModifiers())) {
                        continue;
                    }
                    if (Modifier.isPrivate(f.getModifiers()) && !curPrivate) {
                        continue;
                    }
                    if (!fieldList.contains(f)) {
                        fieldList.add(f);
                    }
                }
                curPrivate = false;
            }

            // 放入缓存
            fields = fieldList.toArray(new Field[fieldList.size()]);
            // return fields;
            fieldCache.put(clazz, fields);
        }

        return fields;
    }

}
