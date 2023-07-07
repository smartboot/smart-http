package org.smartboot.http.restful.parameter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * The Class-Type default value resolver.
 *
 * @author qinluo
 * @date 2022-08-22 09:28:00
 * @since 1.0.0
 */
public class DefaultValueResolver {

    /**
     * The container that store class-defaultValue mappings.
     */
    private static final Map<Class<?>, Object> DEFAULT_VALUE_MAP = new HashMap<>(32);

    /**
     * The container that store primitive-class and wrapper-class mappings.
     */
    private static final Map<Class<?>, Class<?>> WRAP_CLASS_MAP = new ConcurrentHashMap<>(32);

    static {
        // primitive classes.
        DEFAULT_VALUE_MAP.put(int.class, 0);
        DEFAULT_VALUE_MAP.put(byte.class, (byte)0);
        DEFAULT_VALUE_MAP.put(short.class, (short)0);
        DEFAULT_VALUE_MAP.put(long.class, 0L);
        DEFAULT_VALUE_MAP.put(float.class, 0.0F);
        DEFAULT_VALUE_MAP.put(double.class, 0.0D);
        DEFAULT_VALUE_MAP.put(char.class, (char)0);
        DEFAULT_VALUE_MAP.put(boolean.class, false);

        // wrapper classes
        DEFAULT_VALUE_MAP.put(Integer.class, null);
        DEFAULT_VALUE_MAP.put(Byte.class, null);
        DEFAULT_VALUE_MAP.put(Short.class, null);
        DEFAULT_VALUE_MAP.put(Long.class, null);
        DEFAULT_VALUE_MAP.put(Float.class, null);
        DEFAULT_VALUE_MAP.put(Double.class, null);
        DEFAULT_VALUE_MAP.put(Character.class, null);
        DEFAULT_VALUE_MAP.put(Boolean.class, null);

        // object classes
        DEFAULT_VALUE_MAP.put(String.class, null);

        // primitive-class and wrapper-class mappings
        WRAP_CLASS_MAP.put(Integer.class, int.class);
        WRAP_CLASS_MAP.put(Byte.class, byte.class);
        WRAP_CLASS_MAP.put(Short.class, short.class);
        WRAP_CLASS_MAP.put(Long.class, long.class);
        WRAP_CLASS_MAP.put(Float.class, float.class);
        WRAP_CLASS_MAP.put(Double.class, double.class);
        WRAP_CLASS_MAP.put(Character.class, char.class);
        WRAP_CLASS_MAP.put(Boolean.class, boolean.class);
    }

    /**
     * Resolve default value with type.
     *
     * @param type type.
     * @return     default value, may null.
     */
    public static Object resolve(Class<?> type) {
        return resolve(type, false);
    }

    /**
     * Resolve default value with field.
     *
     * @param field field.
     * @return      default value, may null.
     */
    public static Object resolve(Field field) {
        if (field == null) {
            return null;
        }
        return resolve(field.getType(), false);
    }

    /**
     * Resolve default value with field.
     *
     * @param field field.
     * @return      default value, may null.
     */
    public static Object resolve(Field field, boolean unpacked) {
        if (field == null) {
            return null;
        }
        return resolve(field.getType(), unpacked);
    }

    /**
     * Resolve default value with type.
     *
     * @param type     type.
     * @param unpacked if type is wrapper class, determine whether unpack it.
     * @return         default value, may null.
     */
    public static Object resolve(Class<?> type, boolean unpacked) {
        Object defaultValue = DEFAULT_VALUE_MAP.get(type);
        if (defaultValue == null && unpacked) {
            Class<?> primitiveType = WRAP_CLASS_MAP.get(type);
            if (primitiveType != null) {
                defaultValue = DEFAULT_VALUE_MAP.get(primitiveType);
            }
        }
        return defaultValue;
    }
}

