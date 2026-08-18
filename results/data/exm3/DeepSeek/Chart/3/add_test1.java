// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCreateCopy5() throws CloneNotSupportedException {
    TimeSeries s1 = new TimeSeries("S1");
    s1.add(new Year(2009), 100.0);
    s1.add(new Year(2010), 101.0);
    s1.add(new Year(2011), 102.0);
    try {
        s1.createCopy(0,3);
        fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
        // expected
    }
}
