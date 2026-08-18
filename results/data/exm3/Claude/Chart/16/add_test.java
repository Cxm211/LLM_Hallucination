// org/jfree/data/category/junit/DefaultIntervalCategoryDatasetTests.java
public void testConstructorWithNonEmptySeriesKeys() {
    Comparable[] seriesKeys = new Comparable[] {"S1", "S2"};
    double[][] starts = {{1.0, 2.0}, {3.0, 4.0}};
    double[][] ends = {{2.0, 3.0}, {4.0, 5.0}};
    DefaultIntervalCategoryDataset d = new DefaultIntervalCategoryDataset(seriesKeys, null, starts, ends);
    assertEquals(2, d.getRowCount());
    assertEquals(2, d.getColumnCount());
    assertEquals("S1", d.getRowKey(0));
    assertEquals("S2", d.getRowKey(1));
}