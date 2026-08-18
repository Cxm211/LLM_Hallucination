    public static Locale toLocale(String str) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len < 2) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        if (len == 2) {
            return new Locale(str, "");
        }
        if (str.charAt(2) != '_') {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        if (len >= 4 && str.charAt(3) == '_') {
            // empty country
            if (len == 4) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            String variant = str.substring(4);
            return new Locale(str.substring(0, 2), "", variant);
        } else {
            if (len < 5) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            char ch3 = str.charAt(3);
            char ch4 = str.charAt(4);
            if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            String country = str.substring(3, 5);
            if (len == 5) {
                return new Locale(str.substring(0, 2), country, "");
            }
            if (len < 7) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            if (str.charAt(5) != '_') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            String variant = str.substring(6);
            return new Locale(str.substring(0, 2), country, variant);
        }
    }