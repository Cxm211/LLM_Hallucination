// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCreateCopy4() throws CloneNotSupportedException {
    TimeSeries s1 = new TimeSeries("S1");
    try {
        s1.createCopy(0,0);
        fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
        // expected
    }
}
