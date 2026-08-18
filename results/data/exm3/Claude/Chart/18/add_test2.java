// org/jfree/data/category/junit/DefaultCategoryDatasetTests.java
public void testRemoveColumnMultipleRows() {
    DefaultCategoryDataset d = new DefaultCategoryDataset();
    d.addValue(1.0, "R1", "C1");
    d.addValue(2.0, "R1", "C2");
    d.addValue(3.0, "R2", "C1");
    d.addValue(4.0, "R2", "C2");
    d.removeColumn("C1");
    assertEquals(2.0, d.getValue("R1", "C2").doubleValue(), EPSILON);
    assertEquals(4.0, d.getValue("R2", "C2").doubleValue(), EPSILON);
    d.addValue(5.0, "R1", "C1");
    assertEquals(5.0, d.getValue("R1", "C1").doubleValue(), EPSILON);
}