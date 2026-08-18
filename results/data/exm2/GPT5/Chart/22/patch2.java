public void removeRow(Comparable rowKey) {
        if (rowKey == null) {
            throw new IllegalArgumentException("Null 'rowKey' argument.");
        }
        int index = getRowIndex(rowKey);
        if (index < 0) {
            throw new UnknownKeyException("Row key (" + rowKey + ") not recognised.");
        }
        removeRow(index);
    }