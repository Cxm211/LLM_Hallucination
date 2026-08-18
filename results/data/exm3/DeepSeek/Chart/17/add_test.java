// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCloneEmptySeriesProperties() {
    TimeSeries s1 = new TimeSeries("TestSeries");
    TimeSeries s2 = null;
    try {
        s2 = (TimeSeries) s1.clone();
    } catch (CloneNotSupportedException e) {
        e.printStackTrace();
    }
    assertNotNull(s2);
    assertEquals(s1.getName(), s2.getName());
}
