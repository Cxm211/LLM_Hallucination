    private <T> T withOuterClass(Class<T> cls) {
        try {
            Constructor<T> c = null;
            // First try exact match for backward compatibility
            try {
                c = cls.getDeclaredConstructor(outerClassInstance.getClass());
            } catch (NoSuchMethodException e) {
                // Search for a constructor with a single parameter that is assignable from outer instance
                for (Constructor<?> candidate : cls.getDeclaredConstructors()) {
                    Class<?>[] paramTypes = candidate.getParameterTypes();
                    if (paramTypes.length == 1 && paramTypes[0].isAssignableFrom(outerClassInstance.getClass())) {
                        @SuppressWarnings("unchecked")
                        Constructor<T> typed = (Constructor<T>) candidate;
                        c = typed;
                        break;
                    }
                }
                if (c == null) {
                    throw e; // rethrow the original exception
                }
            }
            c.setAccessible(true);
            return c.newInstance(outerClassInstance);
        } catch (Exception e) {
            throw paramsException(cls, e);
        }
    }