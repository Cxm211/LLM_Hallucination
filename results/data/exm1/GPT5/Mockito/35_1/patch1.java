public static <T> T eq(T value) {
        reportMatcher(new Equals(value));
        return value;
    }