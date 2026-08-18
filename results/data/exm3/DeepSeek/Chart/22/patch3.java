public void removeColumn(Comparable columnKey) {
        if (columnKey == null) {
            throw new IllegalArgumentException("Null 'columnKey' argument.");
        }
        int index = getColumnIndex(columnKey);
        if (index < 0) {
            throw new UnknownKeyException("Column key (" + columnKey 
                    + ") not recognised.");
        }
        for (int i = this.rows.size() - 1; i >= 0; i--) {
            KeyedObjects rowData = (KeyedObjects) this.rows.get(i);
            rowData.removeValue(columnKey);
            boolean allNull = true;
            for (int j = 0; j < rowData.getItemCount(); j++) {
                if (rowData.getObject(j) != null) {
                    allNull = false;
                    break;
                }
            }
            if (allNull) {
                this.rows.remove(i);
                this.rowKeys.remove(i);
            }
        }
        this.columnKeys.remove(columnKey);
    }