// org/jfree/data/general/junit/DatasetUtilitiesTests.java
public void testIterateDomainBounds_AllNaN() {
    XYIntervalSeriesCollection d = new XYIntervalSeriesCollection();
    XYIntervalSeries s = new XYIntervalSeries("S1");
    s.add(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    s.add(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    d.addSeries(s);
    Range r = DatasetUtilities.iterateDomainBounds(d);
    assertNull(r);
}