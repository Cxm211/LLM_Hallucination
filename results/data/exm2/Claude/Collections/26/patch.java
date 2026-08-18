private Object readResolve() {
    if (keys != null) {
        calculateHashCode(keys);
    }
    return this;
}