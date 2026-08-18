private <T> T withOuterClass(Class<T> cls) {
        try {
            Class<?> outerCls = outerClassInstance.getClass();
            Constructor<?>[] constructors = cls.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] params = constructor.getParameterTypes();
                if (params.length == 1 && params[0].isAssignableFrom(outerCls)) {
                    constructor.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    T instance = (T) constructor.newInstance(outerClassInstance);
                    return instance;
                }
            }
            throw new NoSuchMethodException("No matching constructor found for outer class: " + outerCls.getName());
        } catch (Exception e) {
            throw paramsException(cls, e);
        }
    }