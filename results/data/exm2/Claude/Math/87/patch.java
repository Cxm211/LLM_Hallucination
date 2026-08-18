private Integer getBasicRow(final int col) {
    Integer row = null;
    for (int i = getNumObjectiveFunctions(); i < getHeight(); i++) {
        final double entry = getEntry(i, col);
        if (MathUtils.equals(entry, 1.0, epsilon) && row == null) {
            row = i;
        } else if (!MathUtils.equals(entry, 0.0, epsilon)) {
            return null;
        }
    }
    return row;
}