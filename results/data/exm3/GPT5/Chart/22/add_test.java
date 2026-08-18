// org/jfree/data/junit/KeyedObjects2DTests.java::testGetValueByKey
public void testRemoveValueUnknownKeys() {
        KeyedObjects2D data = new KeyedObjects2D();
        data.setObject("Obj1", "R1", "C1");
        boolean pass = false;
        try {
            data.removeObject("R1", "XX");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
        pass = false;
        try {
            data.removeObject("XX", "C1");
        }
        catch (UnknownKeyException e) {
            pass = true;
        }
        assertTrue(pass);
    }