    public static boolean areEqual(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        Class<?> clazz1 = o1.getClass();
        Class<?> clazz2 = o2.getClass();
        if (clazz1.isArray() && clazz2.isArray()) {
            int length1 = java.lang.reflect.Array.getLength(o1);
            int length2 = java.lang.reflect.Array.getLength(o2);
            if (length1 != length2) {
                return false;
            }
            for (int i = 0; i < length1; i++) {
                Object e1 = java.lang.reflect.Array.get(o1, i);
                Object e2 = java.lang.reflect.Array.get(o2, i);
                if (!areEqual(e1, e2)) {
                    return false;
                }
            }
            return true;
        }
        return o1.equals(o2);
    }