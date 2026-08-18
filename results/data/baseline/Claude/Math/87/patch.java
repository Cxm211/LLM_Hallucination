private Integer getBasicRow(final int col) {
    Integer row = null;
    for (int i = getNumObjectiveFunctions(); i < getHeight(); i++) {
        if (!MathUtils.equals(getEntry(i, col), 0.0, epsilon)) {
            if (row == null) {
                row = i;
            } else {
                return null;
            }
        }
    }
    if (row != null && !MathUtils.equals(getEntry(row, col), 1.0, epsilon)) {
        return null;
    }
    return row;
}