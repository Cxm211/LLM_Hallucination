    public static <T> T same(T value) {
        reportMatcher(new Same(value));
        return DefaultReturnValue.forClass(value != null ? (Class<T>) value.getClass() : null);
    }