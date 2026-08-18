public void removeColumn(Comparable columnKey) {
        if (!this.columnKeys.contains(columnKey)) {
            throw new UnknownKeyException("Unknown columnKey: " + columnKey);
        }
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
                rowData.removeValue(columnKey);
        }
        this.columnKeys.remove(columnKey);
    }