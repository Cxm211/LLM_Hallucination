private <T> T withOuterClass(Class<T> cls) {
        try {
            Class<?> outerType = cls.getDeclaringClass();
            Constructor<T> c = cls.getDeclaredConstructor(outerType);
            c.setAccessible(true);
            return c.newInstance(outerClassInstance);
        } catch (Exception e) {
            throw paramsException(cls, e);
        }
    }