public DefaultPrettyPrinter createInstance() {
    if (getClass() != DefaultPrettyPrinter.class) {
        throw new IllegalStateException("Failed createInstance(): "+getClass().getName()
                +" does not override createInstance(); it has to");
    }
    return new DefaultPrettyPrinter(this);
}