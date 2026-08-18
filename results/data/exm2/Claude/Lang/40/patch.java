public static boolean containsIgnoreCase(String str, String searchStr) {
    if (str == null || searchStr == null) {
        return false;
    }
    return contains(str.toLowerCase(Locale.ENGLISH), searchStr.toLowerCase(Locale.ENGLISH));
}