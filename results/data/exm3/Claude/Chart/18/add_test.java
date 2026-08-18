// org/jfree/data/junit/DefaultKeyedValuesTests.java
public void testRemoveValueAtBeginning() {
    DefaultKeyedValues v = new DefaultKeyedValues();
    v.addValue("K1", 1.0);
    v.addValue("K2", 2.0);
    v.addValue("K3", 3.0);
    v.removeValue("K1");
    assertEquals(-1, v.getIndex("K1"));
    assertEquals(0, v.getIndex("K2"));
    assertEquals(1, v.getIndex("K3"));
    assertEquals(2.0, v.getValue("K2").doubleValue(), EPSILON);
    assertEquals(3.0, v.getValue("K3").doubleValue(), EPSILON);
}