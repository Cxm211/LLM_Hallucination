protected boolean _primitiveAndWrapper(Class<?> c1, Class<?> c2) {
        if (c1.isPrimitive()) {
            return _primitiveAndWrapper(c2, c1);
        }
        if (c2.isPrimitive()) {
            return (c1 == Boolean.class && c2 == Boolean.TYPE)
                || (c1 == Byte.class && c2 == Byte.TYPE)
                || (c1 == Short.class && c2 == Short.TYPE)
                || (c1 == Character.class && c2 == Character.TYPE)
                || (c1 == Integer.class && c2 == Integer.TYPE)
                || (c1 == Long.class && c2 == Long.TYPE)
                || (c1 == Float.class && c2 == Float.TYPE)
                || (c1 == Double.class && c2 == Double.TYPE);
        }
        return false;
    }