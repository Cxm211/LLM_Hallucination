private Integer getBasicRow(final int col) {
        Integer row = null;
        for (int i = getNumObjectiveFunctions(); i < getHeight(); i++) {
            final double entry = getEntry(i, col);
            if (MathUtils.equals(entry, 1.0, epsilon)) {
                if (row == null) {
                    row = i;
                } else {
                    // More than one '1' in the column: not a basic column
                    return null;
                }
            } else if (!MathUtils.equals(entry, 0.0, epsilon)) {
                // Found a non-zero entry that is not 1: not a basic column
                return null;
            }
        }
        return row;
    }