public static <T> T isA(Class<T> clazz) {
        reportMatcher(new InstanceOf(clazz));
        Object defaultValue = null;
        if (clazz == Boolean.class) {
            defaultValue = Boolean.FALSE;
        } else if (clazz == Byte.class) {
            defaultValue = (byte) 0;
        } else if (clazz == Short.class) {
            defaultValue = (short) 0;
        } else if (clazz == Integer.class) {
            defaultValue = 0;
        } else if (clazz == Long.class) {
            defaultValue = 0L;
        } else if (clazz == Float.class) {
            defaultValue = 0F;
        } else if (clazz == Double.class) {
            defaultValue = 0D;
        } else if (clazz == Character.class) {
            defaultValue = '\0';
        }
        return (T) defaultValue;
    }