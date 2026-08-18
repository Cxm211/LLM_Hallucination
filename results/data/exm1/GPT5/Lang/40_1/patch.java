public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        int searchLen = searchStr.length();
        int strLen = str.length();
        if (searchLen == 0) {
            return true;
        }
        for (int i = 0; i <= strLen - searchLen; i++) {
            if (str.regionMatches(true, i, searchStr, 0, searchLen)) {
                return true;
            }
        }
        return false;
    }