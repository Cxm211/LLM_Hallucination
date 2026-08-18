// buggy function
    public Object getObject(Comparable rowKey, Comparable columnKey) {
        if (rowKey == null) {
            throw new IllegalArgumentException("Null 'rowKey' argument.");
        }
        if (columnKey == null) {
            throw new IllegalArgumentException("Null 'columnKey' argument.");
        }
        int row = this.rowKeys.indexOf(rowKey);
        if (row < 0) {
            throw new UnknownKeyException("Row key (" + rowKey 
                    + ") not recognised.");
        }
        int column = this.columnKeys.indexOf(columnKey);
        if (column < 0) {
            throw new UnknownKeyException("Column key (" + columnKey 
                    + ") not recognised.");
        }
        if (row >= 0) {
        KeyedObjects rowData = (KeyedObjects) this.rows.get(row);
            return rowData.getObject(columnKey);
        }
        else {
            return null;
        }
    }

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
        
        
    }

    public void removeRow(Comparable rowKey) {
        int index = getRowIndex(rowKey);
        removeRow(index);
    }

    public void removeColumn(Comparable columnKey) {
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

// trigger testcase
// org/jfree/data/junit/KeyedObjects2DTests.java::testGetValueByKey
public void testGetValueByKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.addObject("Obj1", "R1", "C1");
        data.addObject("Obj2", "R2", "C2");
        assertEquals("Obj1", data.getObject("R1", "C1"));
        assertEquals("Obj2", data.getObject("R2", "C2"));
        assertNull(data.getObject("R1", "C2"));
        assertNull(data.getObject("R2", "C1"));
        
        // check invalid indices
        boolean pass = false;
        try {
            data.getObject("XX", "C1");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            data.getObject("R1", "XX");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            data.getObject("XX", "C1");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);

        pass = false;
        try {
            data.getObject("R1", "XX");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org/jfree/data/junit/KeyedObjects2DTests.java::testRemoveColumnByIndex
public void testRemoveColumnByIndex() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        data.removeColumn(0);
        assertEquals(1, data.getColumnCount());
        assertEquals("Obj2", data.getObject(1, 0));
        
        // try negative column index
        boolean pass = false;
        try {
            data.removeColumn(-1);
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
        
        // try column index too high
        pass = false;
        try {
            data.removeColumn(data.getColumnCount());
        }
        catch (IndexOutOfBoundsException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org/jfree/data/junit/KeyedObjects2DTests.java::testRemoveColumnByKey
public void testRemoveColumnByKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        data.removeColumn("C2");
        assertEquals(1, data.getColumnCount());
        assertEquals("Obj1", data.getObject(0, 0));
        
        // try unknown column key
        boolean pass = false;
        try {
            data.removeColumn("XXX");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
        
        // try null column key
        pass = false;
        try {
            data.removeColumn(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org/jfree/data/junit/KeyedObjects2DTests.java::testRemoveRowByKey
public void testRemoveRowByKey() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        data.removeRow("R2");
        assertEquals(1, data.getRowCount());
        assertEquals("Obj1", data.getObject(0, 0));
        
        // try unknown row key
        boolean pass = false;
        try {
            data.removeRow("XXX");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
        
        // try null row key
        pass = false;
        try {
            data.removeRow(null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }

// org/jfree/data/junit/KeyedObjects2DTests.java::testRemoveValue
public void testRemoveValue() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        data.removeObject("R2", "C2");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("Obj1", data.getObject(0, 0));
    }

// org/jfree/data/junit/KeyedObjects2DTests.java::testSetObject
public void testSetObject() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        data.setObject("Obj2", "R2", "C2");
        assertEquals("Obj1", data.getObject("R1", "C1"));
        assertEquals("Obj2", data.getObject("R2", "C2"));
        assertNull(data.getObject("R1", "C2"));
        assertNull(data.getObject("R2", "C1"));
        
        // confirm overwriting an existing value
        data.setObject("ABC", "R2", "C2");
        assertEquals("ABC", data.getObject("R2", "C2"));
        
        // try null keys
        boolean pass = false;
        try {
            data.setObject("X", null, "C1");
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
        
        pass = false;
        try {
            data.setObject("X", "R1", null);
        }
        catch (IllegalArgumentException e) {
            pass = true;
        }
        assertTrue(pass);
    }
