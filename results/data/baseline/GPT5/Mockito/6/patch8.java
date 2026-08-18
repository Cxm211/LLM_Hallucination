    public static <T> T anyObject() {
        reportMatcher(Any.ANY);
        return (T) null;
    }
