public void removeRow(Comparable rowKey) {
        if (rowKey == null) {
            throw new IllegalArgumentException("Null 'rowKey' argument.");
        }
        int index = getRowIndex(rowKey);
        removeRow(index);
    }