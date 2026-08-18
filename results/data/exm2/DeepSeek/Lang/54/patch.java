public static Locale toLocale(String str) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 2) {
            char ch0 = str.charAt(0);
            char ch1 = str.charAt(1);
            if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            return new Locale(str, "");
        }
        if (len < 5) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        if (str.charAt(2) != '_') {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        if (str.charAt(3) == '_') {
            // double underscore, empty country
            String language = str.substring(0, 2);
            String country = "";
            String variant = str.substring(4);
            return new Locale(language, country, variant);
        } else {
            char ch3 = str.charAt(3);
            char ch4 = str.charAt(4);
            if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            String language = str.substring(0, 2);
            String country = str.substring(3, 5);
            if (len == 5) {
                return new Locale(language, country, "");
            } else {
                if (str.charAt(5) != '_') {
                    throw new IllegalArgumentException("Invalid locale format: " + str);
                }
                String variant = str.substring(6);
                return new Locale(language, country, variant);
            }
        }
    }