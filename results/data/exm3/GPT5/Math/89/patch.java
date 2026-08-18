public void addValue(Object v) {
            if (!(v instanceof Comparable)) {
                throw new IllegalArgumentException("Value is not comparable.");
            }
            addValue((Comparable<?>) v);
    }