private Object readResolve() {
        calculateHashCode(keys);
        calculateHashCode(values);
        return this;
    }