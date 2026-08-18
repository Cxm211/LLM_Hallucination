// org/jfree/data/junit/KeyedObjects2DTests.java
public void testGetObjectMultipleRowsSameColumn() {
    KeyedObjects2D data = new KeyedObjects2D();
    data.addObject("ObjA", "R1", "C1");
    data.addObject("ObjB", "R2", "C1");
    data.addObject("ObjC", "R3", "C1");
    assertEquals("ObjA", data.getObject("R1", "C1"));
    assertEquals("ObjB", data.getObject("R2", "C1"));
    assertEquals("ObjC", data.getObject("R3", "C1"));
}