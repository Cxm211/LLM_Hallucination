    public void removeObject(Comparable rowKey, Comparable columnKey) {
        setObject(null, rowKey, columnKey);
        
        // 1. check whether the row is now empty.
        int rowIndex = getRowIndex(rowKey);
        if (rowIndex >= 0) {
            boolean allNull = true;
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
        }
        
        // 2. check whether the column is now empty.
        int columnIndex = getColumnIndex(columnKey);
        if (columnIndex >= 0) {
            boolean allNullCol = true;
            for (int r = 0, rCount = this.rows.size(); r < rCount; r++) {
                KeyedObjects rowData = (KeyedObjects) this.rows.get(r);
                Object val;
                try {
                    val = rowData.getObject(columnKey);
                } catch (UnknownKeyException e) {
                    val = null;
                }
                if (val != null) {
                    allNullCol = false;
                    break;
                }
            }
            if (allNullCol) {
                // remove the column key from each row and from the columnKeys list
                for (int r = 0, rCount = this.rows.size(); r < rCount; r++) {
                    KeyedObjects rowData = (KeyedObjects) this.rows.get(r);
                    rowData.removeValue(columnKey);
                }
                this.columnKeys.remove(columnKey);
            }
        }
        
    }