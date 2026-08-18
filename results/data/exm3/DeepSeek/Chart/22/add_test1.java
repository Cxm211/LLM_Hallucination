// org/jfree/data/junit/KeyedObjects2DTests.java
public void testRemoveColumnRemovesEmptyRows() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("A", "R1", "C1");
        data.setObject("B", "R2", "C2");
        data.removeColumn("C2");
        assertEquals(1, data.getRowCount());
        assertEquals(1, data.getColumnCount());
        assertEquals("A", data.getObject("R1", "C1"));
        boolean pass = false;
        try {
            data.getObject("R2", "C1");
        } catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
    }
