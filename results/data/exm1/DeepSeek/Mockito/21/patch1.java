private <T> T withOuterClass(Class<T> cls) {
        try {
            Constructor<T> c = cls.getDeclaredConstructor(cls.getEnclosingClass());
            return c.newInstance(outerClassInstance);
        } catch (Exception e) {
            throw paramsException(cls, e);
        }
    }