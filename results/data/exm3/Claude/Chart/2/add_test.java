// org/jfree/data/general/junit/DatasetUtilitiesTests.java
public void testIterateDomainBounds_EmptyDataset() {
    XYIntervalSeriesCollection d = new XYIntervalSeriesCollection();
    Range r = DatasetUtilities.iterateDomainBounds(d);
    assertNull(r);
}