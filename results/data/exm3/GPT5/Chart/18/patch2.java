public void removeColumn(Comparable columnKey) {
        if (!this.columnKeys.contains(columnKey)) {
            throw new UnknownKeyException("The column key (" + columnKey + ") is not recognised.");
        }
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
            if (rowData.getIndex(columnKey) >= 0) {
                rowData.removeValue(columnKey);
            }
        }
        this.columnKeys.remove(columnKey);
    }