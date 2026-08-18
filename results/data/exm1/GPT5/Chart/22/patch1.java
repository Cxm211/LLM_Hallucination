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
            boolean columnAllNull = true;
            for (int r = 0; r < this.rows.size(); r++) {
                KeyedObjects rowData = (KeyedObjects) this.rows.get(r);
                Object v;
                try {
                    v = rowData.getObject(columnKey);
                } catch (UnknownKeyException e) {
                    v = null; // treat as null if key not present in this row
                }
                if (v != null) {
                    columnAllNull = false;
                    break;
                }
            }
            if (columnAllNull) {
                removeColumn(columnKey);
            }
        }
        
    }