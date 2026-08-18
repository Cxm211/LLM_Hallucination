private <T> T withOuterClass(Class<T> cls) {
        try {
            Class<?> outerType = outerClassInstance.getClass();
            try {
                Constructor<T> exact = cls.getDeclaredConstructor(outerType);
                exact.setAccessible(true);
                return exact.newInstance(outerClassInstance);
            } catch (NoSuchMethodException ignored) {
                for (Constructor<?> cons : cls.getDeclaredConstructors()) {
                    Class<?>[] params = cons.getParameterTypes();
                    if (params.length == 1 && params[0].isAssignableFrom(outerType)) {
                        @SuppressWarnings("unchecked")
                        Constructor<T> match = (Constructor<T>) cons;
                        match.setAccessible(true);
                        return match.newInstance(outerClassInstance);
                    }
                }
                throw new NoSuchMethodException("No matching outer-class constructor found");
            }
        } catch (Exception e) {
            throw paramsException(cls, e);
        }
    }