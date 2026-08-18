    private Object readResolve() {
        this.hashCode = calculateHashCode(keys);
        return this;
    }