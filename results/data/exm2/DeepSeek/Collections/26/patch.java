    private Object readResolve() {
        hashCode = calculateHashCode(keys);
        return this;
    }