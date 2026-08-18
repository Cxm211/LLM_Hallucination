private Integer getBasicRow(final int col, boolean ignoreObjectiveRows) {
        Integer row = null;
        int start = ignoreObjectiveRows ? getNumObjectiveFunctions() : 0;
        for (int i = start; i < getHeight(); i++) {
            double entry = getEntry(i, col);
            if (MathUtils.equals(entry, 1.0, epsilon) && (row == null)) {
                row = i;
            } else if (!MathUtils.equals(entry, 0.0, epsilon)) {
                return null;
            }
        }
        return row;
    }