public static boolean containsNone(CharSequence cs, char[] searchChars) {
        if (cs == null || searchChars == null) {
            return true;
        }
        return indexOfAny(cs, searchChars) == INDEX_NOT_FOUND;
    }