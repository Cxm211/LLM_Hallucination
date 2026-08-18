// buggy code
    public <T> T newInstance(Class<T> cls) {
        if (outerClassInstance == null) {
            return noArgConstructor(cls);
        }
        return withOuterClass(cls);
    }

    private <T> T withOuterClass(Class<T> cls) {
        try {
            //this is kind of overengineered because we don't need to support more params
            //however, I know we will be needing it :)
            Constructor<T> c = cls.getDeclaredConstructor(outerClassInstance.getClass());
            return c.newInstance(outerClassInstance);
        } catch (Exception e) {
            throw paramsException(cls, e);
        }
    }

    private static <T> InstantationException paramsException(Class<T> cls, Exception e) {
        return new InstantationException("Unable to create mock instance of '"
                + cls.getSimpleName() + "'.\nPlease ensure that the outer instance has correct type and that the target class has parameter-less constructor.", e);
    }

