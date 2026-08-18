// buggy code
    private Object readResolve() {
        calculateHashCode(keys);
        return this;
    }

