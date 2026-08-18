    private <T> T withOuterClass(Class<T> cls) {
        try {
            //this is kind of overengineered because we don't need to support more params
            //however, I know we will be needing it :)
            Class<?> enclosingClass = cls.getEnclosingClass();
            if (enclosingClass == null) {
                enclosingClass = outerClassInstance.getClass();
            }
            Constructor<T> c = cls.getDeclaredConstructor(enclosingClass);
            return c.newInstance(outerClassInstance);
        } catch (Exception e) {
            throw paramsException(cls, e);
        }
    }