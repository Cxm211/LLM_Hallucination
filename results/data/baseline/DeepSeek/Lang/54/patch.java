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
    int pos = str.indexOf('_', 3);
    String country;
    String variant;
    if (pos == -1) {
        // no second underscore, so country must be two letters
        if (len != 5) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        char ch3 = str.charAt(3);
        char ch4 = str.charAt(4);
        if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        country = str.substring(3, 5);
        variant = "";
    } else {
        // there is a second underscore
        country = str.substring(3, pos);
        if (country.length() != 0 && (country.length() != 2 || 
            !(country.charAt(0) >= 'A' && country.charAt(0) <= 'Z' && 
              country.charAt(1) >= 'A' && country.charAt(1) <= 'Z'))) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        variant = str.substring(pos + 1);
    }
    return new Locale(str.substring(0, 2), country, variant);
}