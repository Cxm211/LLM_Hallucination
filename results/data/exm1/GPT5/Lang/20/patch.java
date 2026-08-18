public static String join(Object[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = (endIndex - startIndex);
        if (noOfItems <= 0) {
            return EMPTY;
        }
        StringBuilder buf = new StringBuilder((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + 1);
        boolean firstAppended = false;
        for (int i = startIndex; i < endIndex; i++) {
            if (array[i] != null) {
                if (firstAppended) {
                    buf.append(separator);
                }
                buf.append(array[i]);
                firstAppended = true;
            }
        }
        return buf.toString();
    }