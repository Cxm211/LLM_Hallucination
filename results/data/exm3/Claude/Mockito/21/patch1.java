private <T> T withOuterClass(Class<T> cls) {
    try {
        //this is kind of overengineered because we don't need to support more params
        //however, I know we will be needing it :)
        Class<?> outerClass = outerClassInstance.getClass();
        Constructor<T> c = null;
        
        // Try to find a constructor that accepts the outer class or its superclasses
        while (outerClass != null && c == null) {
            try {
                c = cls.getDeclaredConstructor(outerClass);
            } catch (NoSuchMethodException e) {
                // Move to superclass if constructor not found
                outerClass = outerClass.getSuperclass();
            }
        }
        
        if (c == null) {
            throw new NoSuchMethodException("No suitable constructor found");
        }
        
        return c.newInstance(outerClassInstance);
    } catch (Exception e) {
        throw paramsException(cls, e);
    }
}
