    public static boolean areEqual(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        boolean o1Array = isArray(o1);
        boolean o2Array = isArray(o2);
        if (o1Array && o2Array) {
            return areArraysEqual(o1, o2);
        }
        if (o1Array || o2Array) {
            return false;
        }
        return o1.equals(o2);
    }