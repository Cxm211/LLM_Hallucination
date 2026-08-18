// org/jfree/data/category/junit/DefaultIntervalCategoryDatasetTests.java
public void testConstructorWithNonEmptyCategoryKeys() {
    Comparable[] categoryKeys = new Comparable[] {"C1", "C2"};
    double[][] starts = {{1.0, 2.0}, {3.0, 4.0}};
    double[][] ends = {{2.0, 3.0}, {4.0, 5.0}};
    DefaultIntervalCategoryDataset d = new DefaultIntervalCategoryDataset(null, categoryKeys, starts, ends);
    assertEquals(2, d.getRowCount());
    assertEquals(2, d.getColumnCount());
    assertEquals("C1", d.getColumnKey(0));
    assertEquals("C2", d.getColumnKey(1));
}