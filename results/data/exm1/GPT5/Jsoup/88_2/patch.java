public String getValue() {
        if (val == null) return null;
        int len = val.length();
        if (len == 0) return val;
        if (val.charAt(len - 1) == ' ') {
            int i = len - 2;
            int backslashes = 0;
            while (i >= 0 && val.charAt(i) == '\\') { backslashes++; i--; }
            if ((backslashes & 1) == 1) {
                return val; // preserve trailing space if escaped
            }
        }
        return val.trim();
    }