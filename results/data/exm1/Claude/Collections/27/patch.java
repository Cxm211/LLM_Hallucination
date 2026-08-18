public T create() {
            if (!Collection.class.isAssignableFrom(clazz)) {
                throw new UnsupportedOperationException("Cannot instantiate class: " + clazz + ". Only Collection types are supported.");
            }
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (final Exception ex) {
                throw new FunctorException("Cannot instantiate class: " + clazz, ex);
            }
        }