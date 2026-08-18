public static boolean containsAny(CharSequence cs, char[] searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return false;
        }
        return indexOfAny(cs, searchChars) != INDEX_NOT_FOUND;
    }