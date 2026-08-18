public void addValue(Object v) {
    if (v == null || v instanceof Comparable) {
        addValue((Comparable<?>) v);
    } else {
        throw new IllegalArgumentException("Value must be Comparable");
    }
}