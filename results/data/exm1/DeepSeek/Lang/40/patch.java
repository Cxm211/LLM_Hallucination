public static boolean containsIgnoreCase(String str, String searchStr) {
    if (str == null || searchStr == null) {
        return false;
    }
    return contains(str.toUpperCase(java.util.Locale.ENGLISH), searchStr.toUpperCase(java.util.Locale.ENGLISH));
}