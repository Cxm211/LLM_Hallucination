public void addValue(Object v) {
    if (v == null || !(v instanceof Comparable)) {
        throw new IllegalArgumentException("Value must be Comparable");
    }
    addValue((Comparable<?>) v);
}