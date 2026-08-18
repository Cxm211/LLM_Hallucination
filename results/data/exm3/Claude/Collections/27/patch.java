public T create() {
    try {
        if (!Collection.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException("Cannot instantiate class: " + clazz + " (not a Collection)");
        }
        return clazz.newInstance();
    } catch (final UnsupportedOperationException ex) {
        throw ex;
    } catch (final Exception ex) {
        throw new FunctorException("Cannot instantiate class: " + clazz, ex);
    }
}