public void removeObject(Comparable rowKey, Comparable columnKey) {
        if (rowKey == null) {
            throw new IllegalArgumentException("Null 'rowKey' argument.");
        }
        if (columnKey == null) {
            throw new IllegalArgumentException("Null 'columnKey' argument.");
        }
        int rowIndex = getRowIndex(rowKey);
        if (rowIndex < 0) {
            throw new UnknownKeyException("Row key (" + rowKey + ") not recognised.");
        }
        int columnIndex = getColumnIndex(columnKey);
        if (columnIndex < 0) {
            throw new UnknownKeyException("Column key (" + columnKey + ") not recognised.");
        }

        // remove the value from the specified cell (if present)
        KeyedObjects row = (KeyedObjects) this.rows.get(rowIndex);
        int itemIndex = row.getIndex(columnKey);
        if (itemIndex >= 0) {
            row.removeValue(columnKey);
        }
        
        // 1. check whether the row is now empty (no non-null items)
        boolean allNull = true;
        for (int item = 0, itemCount = row.getItemCount(); item < itemCount; item++) {
            if (row.getObject(item) != null) {
                allNull = false;
                break;
            }
        }
        if (allNull) {
            this.rowKeys.remove(rowIndex);
            this.rows.remove(rowIndex);
        }
        
        // 2. check whether the column is now empty across all rows
        boolean colAllNull = true;
        for (int r = 0; r < this.rows.size(); r++) {
            KeyedObjects rdata = (KeyedObjects) this.rows.get(r);
            int cidx = rdata.getIndex(columnKey);
            if (cidx >= 0 && rdata.getObject(cidx) != null) {
                colAllNull = false;
                break;
            }
        }
        if (colAllNull) {
            // remove the column key and any entries for this column in each row
            this.columnKeys.remove(columnKey);
            for (int r = 0; r < this.rows.size(); r++) {
                KeyedObjects rdata = (KeyedObjects) this.rows.get(r);
                int cidx = rdata.getIndex(columnKey);
                if (cidx >= 0) {
                    rdata.removeValue(columnKey);
                }
            }
        }
    }