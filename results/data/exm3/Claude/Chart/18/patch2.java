public void removeColumn(Comparable columnKey) {
    if (!this.columnKeys.contains(columnKey)) {
        throw new UnknownKeyException("Column key not found: " + columnKey);
    }
    Iterator iterator = this.rows.iterator();
    while (iterator.hasNext()) {
        DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
        int index = rowData.getIndex(columnKey);
        if (index >= 0) {
            rowData.removeValue(index);
        }
    }
    this.columnKeys.remove(columnKey);
}