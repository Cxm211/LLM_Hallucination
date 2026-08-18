public T create() {
    try {
        return clazz.newInstance();
    } catch (final InstantiationException | IllegalAccessException ex) {
        throw new FunctorException("Cannot instantiate class: " + clazz, ex);
    }
}