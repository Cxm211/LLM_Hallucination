    public static <T> T[] add(T[] array, T element) {
        if (array == null && element == null) {
            throw new IllegalArgumentException("Array and element cannot both be null");
        }
        Class<?> type;
        if (array != null){
            type = array.getClass().getComponentType();
        } else {
            type = element.getClass();
        }
        @SuppressWarnings("unchecked") // type must be T
        T[] newArray = (T[]) copyArrayGrow1(array, type);
        newArray[newArray.length - 1] = element;
        return newArray;
    }