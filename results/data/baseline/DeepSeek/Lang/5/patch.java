public static Locale toLocale(final String str) {
    if (str == null) {
        return null;
    }
    final int len = str.length();
    if (len < 2) {
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    int firstUnderscore = str.indexOf('_');
    String language;
    if (firstUnderscore == -1) {
        language = str;
        if (language.length() < 2) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        for (int i = 0; i < language.length(); i++) {
            if (!Character.isLowerCase(language.charAt(i))) {
                throw new IllegalArgumentException("Invalid locale format: " + str);
            }
        }
        return new Locale(language);
    }
    language = str.substring(0, firstUnderscore);
    if (language.length() < 2) {
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    for (int i = 0; i < language.length(); i++) {
        if (!Character.isLowerCase(language.charAt(i))) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
    }
    int pos = firstUnderscore + 1;
    if (pos >= len) {
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    if (str.charAt(pos) == '_') {
        String variant = str.substring(pos + 1);
        if (variant.isEmpty()) {
            throw new IllegalArgumentException("Invalid locale format: " + str);
        }
        return new Locale(language, "", variant);
    }
    if (pos + 2 > len) {
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    String country = str.substring(pos, pos + 2);
    if (!Character.isUpperCase(country.charAt(0)) || !Character.isUpperCase(country.charAt(1))) {
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    pos += 2;
    if (pos == len) {
        return new Locale(language, country);
    }
    if (str.charAt(pos) != '_') {
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    String variant = str.substring(pos + 1);
    if (variant.isEmpty()) {
        throw new IllegalArgumentException("Invalid locale format: " + str);
    }
    return new Locale(language, country, variant);
}