public static <T> T[] add(T[] array, T element) {
    Class<?> type;
    if (array != null){
        type = array.getClass();
    } else if (element != null) {
        type = Array.newInstance(element.getClass(), 0).getClass();
    } else {
        type = Object.class;
    }
    @SuppressWarnings("unchecked")
    T[] newArray = (T[]) copyArrayGrow1(array, type);
    newArray[newArray.length - 1] = element;
    return newArray;
}