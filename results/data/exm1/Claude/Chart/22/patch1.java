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
    boolean colAllNull = true;
    for (int r = 0; r < this.rows.size(); r++) {
        KeyedObjects rowData = (KeyedObjects) this.rows.get(r);
        if (rowData.getObject(columnKey) != null) {
            colAllNull = false;
            break;
        }
    }
    
    if (colAllNull) {
        this.columnKeys.remove(columnKey);
        for (int r = 0; r < this.rows.size(); r++) {
            KeyedObjects rowData = (KeyedObjects) this.rows.get(r);
            rowData.removeValue(columnKey);
        }
    }
}