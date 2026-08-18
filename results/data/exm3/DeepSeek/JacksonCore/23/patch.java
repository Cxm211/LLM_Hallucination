    public DefaultPrettyPrinter createInstance() {
        if (getClass() != DefaultPrettyPrinter.class) {
            throw new IllegalStateException("Sub-class " + getClass().getName() + " does not override 'createInstance()' method");
        }
        return new DefaultPrettyPrinter(this);
    }