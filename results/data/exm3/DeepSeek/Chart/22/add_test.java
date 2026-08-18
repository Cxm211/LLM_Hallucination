// org/jfree/data/junit/KeyedObjects2DTests.java
public void testRemoveObjectColumnBecomesEmptyButRowRemains() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("X", "R1", "C1");
        data.setObject("Y", "R1", "C2");
        data.setObject("Z", "R2", "C1");
        data.removeObject("R1", "C2");
        assertEquals(1, data.getColumnCount());
        assertEquals(2, data.getRowCount());
        assertEquals("X", data.getObject("R1", "C1"));
        assertEquals("Z", data.getObject("R2", "C1"));
        boolean pass = false;
        try {
            data.getObject("R1", "C2");
        } catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
    }
