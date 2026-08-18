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
        boolean columnEmpty = true;
        for (Object obj : this.rows) {
            KeyedObjects rowData = (KeyedObjects) obj;
            if (rowData.getObject(columnKey) != null) {
                columnEmpty = false;
                break;
            }
        }
        if (columnEmpty) {
            this.columnKeys.remove(columnKey);
        }
    }