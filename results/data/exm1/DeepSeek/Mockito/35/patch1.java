    public static <T> T eq(T value) {
        reportMatcher(new Equals(value));
        return DefaultReturnValue.forClass(value != null ? (Class<T>) value.getClass() : null);
    }