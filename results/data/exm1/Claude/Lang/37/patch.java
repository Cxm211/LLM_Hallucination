public static <T> T[] addAll(T[] array1, T... array2) {
    if (array1 == null) {
        return clone(array2);
    } else if (array2 == null) {
        return clone(array1);
    }
    final Class<?> type1 = array1.getClass().getComponentType();
    final Class<?> type2 = array2.getClass().getComponentType();
    final Class<?> type;
    if (type1.isAssignableFrom(type2)) {
        type = type1;
    } else if (type2.isAssignableFrom(type1)) {
        type = type2;
    } else {
        throw new IllegalArgumentException("Cannot add two arrays of incompatible types: " + type1 + " and " + type2);
    }
    @SuppressWarnings("unchecked")
    T[] joinedArray = (T[]) Array.newInstance(type, array1.length + array2.length);
    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
    try {
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
    } catch (ArrayStoreException ase) {
        throw new IllegalArgumentException("Cannot add two arrays of incompatible types: " + type1 + " and " + type2, ase);
    }
    return joinedArray;
}