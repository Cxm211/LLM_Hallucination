// org/jfree/data/category/junit/DefaultIntervalCategoryDatasetTests.java
public void testConstructorWithMismatchedSeriesKeysLength() {
    Comparable[] seriesKeys = new Comparable[] {"S1"};
    double[][] starts = {{1.0, 2.0}, {3.0, 4.0}};
    double[][] ends = {{2.0, 3.0}, {4.0, 5.0}};
    boolean exceptionThrown = false;
    try {
        new DefaultIntervalCategoryDataset(seriesKeys, null, starts, ends);
    } catch (IllegalArgumentException e) {
        exceptionThrown = true;
    }
    assertTrue(exceptionThrown);
}