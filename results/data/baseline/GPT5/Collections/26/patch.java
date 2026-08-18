private Object readResolve() {
        this.cachedHashCode = calculateHashCode(keys);
        return this;
    }