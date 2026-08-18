        public T create() {
            if (!java.util.Collection.class.isAssignableFrom(clazz)) {
                throw new UnsupportedOperationException("Unsafe class: " + clazz);
            }
            try {
                return clazz.newInstance();
            } catch (final Exception ex) {
                throw new FunctorException("Cannot instantiate class: " + clazz, ex);
            }
        }