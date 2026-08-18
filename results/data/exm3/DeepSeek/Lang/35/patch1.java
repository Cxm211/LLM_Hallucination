    public static <T> T[] add(T[] array, int index, T element) {
        if (array == null && element == null) {
            throw new IllegalArgumentException("Array and element cannot both be null");
        }
        Class<?> clss = null;
        if (array != null) {
            clss = array.getClass().getComponentType();
        } else {
            clss = element.getClass();
        }
        @SuppressWarnings("unchecked") // the add method creates an array of type clss, which is type T
        final T[] newArray = (T[]) add(array, index, element, clss);
        return newArray;
    }