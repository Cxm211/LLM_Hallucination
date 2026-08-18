// org/jfree/data/junit/DefaultKeyedValuesTests.java
public void testRemoveLastUpdatesIndex() {
    DefaultKeyedValues v = new DefaultKeyedValues();
    v.addValue("K1", 1.0);
    v.addValue("K2", 2.0);
    assertEquals(1, v.getIndex("K2"));
    v.removeValue("K2");
    assertEquals(-1, v.getIndex("K2"));
    assertEquals(0, v.getIndex("K1"));
}
