// org/jfree/data/general/junit/DatasetUtilitiesTests.java
public void testIterateRangeBounds_EmptyDataset() {
    XYIntervalSeriesCollection d = new XYIntervalSeriesCollection();
    Range r = DatasetUtilities.iterateRangeBounds(d);
    assertNull(r);
}