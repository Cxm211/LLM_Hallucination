private <T> T withOuterClass(Class<T> cls) {
        try {
            Constructor<?>[] ctors = cls.getDeclaredConstructors();
            for (Constructor<?> ctor : ctors) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length == 1 && params[0].isAssignableFrom(outerClassInstance.getClass())) {
                    ctor.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Constructor<T> match = (Constructor<T>) ctor;
                    return match.newInstance(outerClassInstance);
                }
            }
            Constructor<T> c = cls.getDeclaredConstructor(outerClassInstance.getClass());
            c.setAccessible(true);
            return c.newInstance(outerClassInstance);
        } catch (Exception e) {
            throw paramsException(cls, e);
        }
    }