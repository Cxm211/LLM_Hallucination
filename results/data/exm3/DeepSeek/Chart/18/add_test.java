// org/jfree/data/junit/DefaultKeyedValuesTests.java
public void testRemoveLastValueAndReAdd() {
    DefaultKeyedValues v = new DefaultKeyedValues();
    v.addValue("K1", 1.0);
    v.addValue("K2", 2.0);
    v.removeValue("K2");
    v.addValue("K2", 3.0);
    assertEquals(3.0, v.getValue("K2").doubleValue(), 0.000000001);
    assertEquals(1.0, v.getValue("K1").doubleValue(), 0.000000001);
}
