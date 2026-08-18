    public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = EMPTY;
        }

        // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
        //           (Assuming that all Strings are roughly equally long)
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
        StringBuilder buf = new StringBuilder(firstLen + separator.length());

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