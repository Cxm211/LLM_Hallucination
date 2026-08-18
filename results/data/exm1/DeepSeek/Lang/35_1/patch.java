public static <T> T[] add(T[] array, T element) {
    Class<?> type;
    if (array != null){
        type = array.getClass().getComponentType();
    } else if (element != null) {
        type = element.getClass();
    } else {
        type = Object.class;
    }
    @SuppressWarnings("unchecked")
    T[] newArray = (T[]) copyArrayGrow1(array, type);
    newArray[newArray.length - 1] = element;
    return newArray;
}