    public void removeObject(Comparable rowKey, Comparable columnKey) {
        setObject(null, rowKey, columnKey);
        
        // 1. check whether the row is now empty.
        boolean allNull = true;
        int rowIndex = getRowIndex(rowKey);
        KeyedObjects row = (KeyedObjects) this.rows.get(rowIndex);

        for (int item = 0, itemCount = row.getItemCount(); item < itemCount; 
             item++) {
            if (row.getObject(item) != null) {
                allNull = false;
                break;
            }
        }
        
        if (allNull) {
            this.rowKeys.remove(rowIndex);
            this.rows.remove(rowIndex);
        }
        
        // 2. check whether the column is now empty.
        int columnIndex = getColumnIndex(columnKey);
        if (columnIndex < 0) {
            throw new UnknownKeyException("Column key (" + columnKey 
                    + ") not recognised.");
        }
        boolean columnAllNull = true;
        for (int r = 0; r < getRowCount(); r++) {
            KeyedObjects row2 = (KeyedObjects) this.rows.get(r);
            if (row2.getObject(columnIndex) != null) {
                columnAllNull = false;
                break;
            }
        }
        if (columnAllNull) {
            removeColumn(columnKey);
        }
    }