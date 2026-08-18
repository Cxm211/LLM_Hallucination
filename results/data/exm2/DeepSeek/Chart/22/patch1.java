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
        int columnIndex = this.columnKeys.indexOf(columnKey);
        if (columnIndex >= 0) {
            boolean columnAllNull = true;
            Iterator rowIter = this.rows.iterator();
            while (rowIter.hasNext()) {
                KeyedObjects rowData = (KeyedObjects) rowIter.next();
                if (rowData.getObject(columnKey) != null) {
                    columnAllNull = false;
                    break;
                }
            }
            if (columnAllNull) {
                // remove this column from each row
                rowIter = this.rows.iterator(); // reset iterator
                while (rowIter.hasNext()) {
                    KeyedObjects rowData = (KeyedObjects) rowIter.next();
                    rowData.removeValue(columnKey);
                }
                this.columnKeys.remove(columnKey);
            }
        }
    }