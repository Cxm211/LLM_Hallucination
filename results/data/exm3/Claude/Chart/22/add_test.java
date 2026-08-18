// org/jfree/data/junit/KeyedObjects2DTests.java
public void testGetObjectAfterRemoval() {
    KeyedObjects2D data = new KeyedObjects2D();
    data.addObject("Obj1", "R1", "C1");
    data.addObject("Obj2", "R1", "C2");
    data.removeObject("R1", "C1");
    assertNull(data.getObject("R1", "C1"));
    assertEquals("Obj2", data.getObject("R1", "C2"));
}