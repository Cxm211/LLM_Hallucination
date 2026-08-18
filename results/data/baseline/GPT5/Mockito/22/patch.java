public static boolean areEqual(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return o1 == null && o2 == null;
        }
        if (isArray(o1) || isArray(o2)) {
            return isArray(o1) && isArray(o2) && areArraysEqual(o1, o2);
        }
        return o1.equals(o2);
    }