    public static String join(Object[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int noOfItems = (endIndex - startIndex);
        if (noOfItems <= 0) {
            return EMPTY;
        }
        
        // Compute length of first element's string representation safely
        Object first = array[startIndex];
        int firstLen;
        if (first == null) {
            firstLen = 16;
        } else {
            String str = String.valueOf(first);
            firstLen = str == null ? 16 : str.length();
        }
        StringBuilder buf = new StringBuilder(firstLen + 1);

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