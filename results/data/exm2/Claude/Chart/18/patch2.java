public void removeColumn(Comparable columnKey) {
    int index = this.columnKeys.indexOf(columnKey);
    if (index < 0) {
        throw new UnknownKeyException("Unknown key: " + columnKey);
    }
    Iterator iterator = this.rows.iterator();
    while (iterator.hasNext()) {
        DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
            rowData.removeValue(columnKey);
    }
    this.columnKeys.remove(columnKey);
}