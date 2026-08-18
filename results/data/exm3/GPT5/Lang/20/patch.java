public static String join(Object[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        // Clamp indices to array bounds
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        int noOfItems = (endIndex - startIndex);
        if (noOfItems <= 0) {
            return EMPTY;
        }

        Object first = array[startIndex];
        StringBuilder buf = new StringBuilder((first == null ? 16 : first.toString().length()) + 1);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }