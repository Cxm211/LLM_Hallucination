private <T> T withOuterClass(Class<T> cls) {
    try {
        for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length == 1 && paramTypes[0].isAssignableFrom(outerClassInstance.getClass())) {
                return (T) constructor.newInstance(outerClassInstance);
            }
        }
        throw new NoSuchMethodException("No suitable constructor found");
    } catch (Exception e) {
        throw paramsException(cls, e);
    }
}