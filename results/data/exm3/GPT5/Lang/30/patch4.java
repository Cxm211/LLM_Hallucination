public static int indexOfAnyBut(String str, String searchChars) {
        if (isEmpty(str) || isEmpty(searchChars)) {
            return INDEX_NOT_FOUND;
        }
        return indexOfAnyBut((CharSequence) str, searchChars.toCharArray());
    }