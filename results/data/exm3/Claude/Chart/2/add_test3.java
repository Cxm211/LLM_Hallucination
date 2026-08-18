// org/jfree/data/general/junit/DatasetUtilitiesTests.java
public void testIterateRangeBounds_AllNaN() {
    XYIntervalSeriesCollection d = new XYIntervalSeriesCollection();
    XYIntervalSeries s = new XYIntervalSeries("S1");
    s.add(1.0, 1.0, 1.0, Double.NaN, Double.NaN, Double.NaN);
    s.add(2.0, 2.0, 2.0, Double.NaN, Double.NaN, Double.NaN);
    d.addSeries(s);
    Range r = DatasetUtilities.iterateRangeBounds(d);
    assertNull(r);
}