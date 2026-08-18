// org/jfree/data/category/junit/DefaultIntervalCategoryDatasetTests.java
public void testConstructorWithMismatchedCategoryKeysLength() {
    Comparable[] categoryKeys = new Comparable[] {"C1"};
    double[][] starts = {{1.0, 2.0}, {3.0, 4.0}};
    double[][] ends = {{2.0, 3.0}, {4.0, 5.0}};
    boolean exceptionThrown = false;
    try {
        new DefaultIntervalCategoryDataset(null, categoryKeys, starts, ends);
    } catch (IllegalArgumentException e) {
        exceptionThrown = true;
    }
    assertTrue(exceptionThrown);
}