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
            return new Locale(str);
        }
        if (len < 3 || str.charAt(2) != '_') {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        // Determine country/region part length: either 2 letters or 3 digits
        if (len == 3) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        String language = str.substring(0, 2);
        // Check if we have only language + country (no variant)
        if (len == 5) {
            // Expect 2-letter country
            char ch3 = str.charAt(3);
            char ch4 = str.charAt(4);
            if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            return new Locale(language, str.substring(3, 5));
        }
        if (len == 6) {
            // Expect 3-digit country
            char ch3 = str.charAt(3);
            char ch4 = str.charAt(4);
            char ch5 = str.charAt(5);
            if (ch3 < '0' || ch3 > '9' || ch4 < '0' || ch4 > '9' || ch5 < '0' || ch5 > '9') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            return new Locale(language, str.substring(3, 6));
        }
        // With variant present
        // First check if country is 2-letter (underscore at index 5)
        if (len >= 7 && str.charAt(5) == '_') {
            char ch3 = str.charAt(3);
            char ch4 = str.charAt(4);
            if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            String country = str.substring(3, 5);
            String variant = str.substring(6);
            if (variant.length() == 0) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            return new Locale(language, country, variant);
        }
        // Else expect 3-digit country with underscore at index 6
        if (len >= 8 && str.charAt(6) == '_') {
            char ch3 = str.charAt(3);
            char ch4 = str.charAt(4);
            char ch5 = str.charAt(5);
            if (ch3 < '0' || ch3 > '9' || ch4 < '0' || ch4 > '9' || ch5 < '0' || ch5 > '9') {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            String country = str.substring(3, 6);
            String variant = str.substring(7);
            if (variant.length() == 0) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
            return new Locale(language, country, variant);
        }
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }