public void removeObject(Comparable rowKey, Comparable columnKey) {
        setObject(null, rowKey, columnKey);
        
        // 1. check whether the row is now empty.
        boolean allNull = true;
        int rowIndex = getRowIndex(rowKey);
        if (rowIndex >= 0) {
            KeyedObjects row = (KeyedObjects) this.rows.get(rowIndex);
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
        }
        
        // 2. check whether the column is now empty.
        int colIndex = getColumnIndex(columnKey);
        if (colIndex >= 0) {
            boolean allColumnNull = true;
            for (int r = 0, rowCount = this.rows.size(); r < rowCount; r++) {
                KeyedObjects rowData = (KeyedObjects) this.rows.get(r);
                if (rowData.getObject(columnKey) != null) {
                    allColumnNull = false;
                    break;
                }
            }
            if (allColumnNull) {
                this.columnKeys.remove(columnKey);
                for (int r = 0, rowCount = this.rows.size(); r < rowCount; r++) {
                    KeyedObjects rowData = (KeyedObjects) this.rows.get(r);
                    rowData.removeValue(columnKey);
                }
            }
        }
        
    }