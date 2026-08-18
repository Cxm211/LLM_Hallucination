public DefaultPrettyPrinter createInstance() {
    if (getClass() != DefaultPrettyPrinter.class) {
        throw new IllegalStateException("Failed `createInstance()`: "
                + getClass().getName() + " does not override method.");
    }
    return new DefaultPrettyPrinter(this);
}