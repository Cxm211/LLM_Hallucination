public T create() {
    try {
        return clazz.getDeclaredConstructor().newInstance();
    } catch (final Exception ex) {
        throw new FunctorException("Cannot instantiate class: " + clazz, ex);
    }
}