    public void removeColumn(Comparable columnKey) {
        if (columnKey == null) {
            throw new IllegalArgumentException("Null 'columnKey' argument.");
        }
        int index = getColumnIndex(columnKey);
        if (index < 0) {
            throw new UnknownKeyException("Column key (" + columnKey 
                    + ") not recognised.");
        }
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            KeyedObjects rowData = (KeyedObjects) iterator.next();
                rowData.removeValue(columnKey);
        }
        this.columnKeys.remove(columnKey);
    }