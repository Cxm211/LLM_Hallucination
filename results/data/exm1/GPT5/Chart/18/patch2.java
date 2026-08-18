public void removeColumn(Comparable columnKey) {
        int c = this.columnKeys.indexOf(columnKey);
        if (c < 0) {
            throw new UnknownKeyException("Unknown columnKey: " + columnKey);
        }
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
            rowData.removeValue(columnKey);
        }
        this.columnKeys.remove(columnKey);
    }