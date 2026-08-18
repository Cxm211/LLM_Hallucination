    private static <T> InstantiationException paramsException(Class<T> cls, Exception e) {
        return new InstantiationException("Unable to create mock instance of '"
                + cls.getSimpleName() + "'.\nPlease ensure that the outer instance has correct type and that the target class has the required constructor.", e);
    }