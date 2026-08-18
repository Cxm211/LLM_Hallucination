public T create() {
    if (clazz != null && !java.util.Collection.class.isAssignableFrom(clazz)) {
        throw new UnsupportedOperationException("Unsafe clazz: " + clazz);
    }
    try {
        return clazz.getDeclaredConstructor().newInstance();
    } catch (final Exception ex) {
        throw new FunctorException("Cannot instantiate class: " + clazz, ex);
    }
}