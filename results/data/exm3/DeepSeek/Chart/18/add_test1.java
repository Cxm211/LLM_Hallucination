// org/jfree/data/junit/DefaultKeyedValues2DTests.java
public void testRemoveLastColumnAndReAdd() {
    DefaultKeyedValues2D d = new DefaultKeyedValues2D();
    d.addValue(1.0, "R1", "C1");
    d.addValue(2.0, "R1", "C2");
    d.addValue(3.0, "R1", "C3");
    d.removeColumn("C3");
    d.addValue(4.0, "R1", "C3");
    assertEquals(4.0, d.getValue("R1", "C3").doubleValue(), 0.000000001);
    assertEquals(1.0, d.getValue("R1", "C1").doubleValue(), 0.000000001);
    assertEquals(2.0, d.getValue("R1", "C2").doubleValue(), 0.000000001);
}
