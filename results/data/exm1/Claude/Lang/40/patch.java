public static boolean containsIgnoreCase(String str, String searchStr) {
    if (str == null || searchStr == null) {
        return false;
    }
    return contains(str.toUpperCase(Locale.ENGLISH), searchStr.toUpperCase(Locale.ENGLISH));
}