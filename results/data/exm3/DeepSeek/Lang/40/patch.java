    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        if (searchStr.length() == 0) {
            return true;
        }
        final int len = searchStr.length();
        final int limit = str.length() - len;
        for (int i = 0; i <= limit; i++) {
            boolean match = true;
            for (int j = 0; j < len; j++) {
                char c1 = str.charAt(i + j);
                char c2 = searchStr.charAt(j);
                if (Character.toUpperCase(c1) != Character.toUpperCase(c2)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }