// org/jfree/data/category/junit/DefaultIntervalCategoryDatasetTests.java
public void testConstructorWithEmptyCategoryKeysArray() {
    Comparable[] categoryKeys = new Comparable[0];
    double[][] starts = new double[0][0];
    double[][] ends = new double[0][0];
    DefaultIntervalCategoryDataset d = new DefaultIntervalCategoryDataset(null, categoryKeys, starts, ends);
    assertEquals(0, d.getRowCount());
    assertEquals(0, d.getColumnCount());
}