        public T create() {
            if (!java.util.Collection.class.isAssignableFrom(clazz)) {
                throw new UnsupportedOperationException("Unsupported class: " + clazz);
            }
            try {
                return clazz.newInstance();
            } catch (final Exception ex) {
                throw new FunctorException("Cannot instantiate class: " + clazz, ex);
            }
        }