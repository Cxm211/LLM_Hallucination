public DefaultPrettyPrinter createInstance() {
    if (getClass() != DefaultPrettyPrinter.class) {
        throw new IllegalStateException(getClass().getName() + " does not override createInstance()");
    }
    return new DefaultPrettyPrinter(this);
}