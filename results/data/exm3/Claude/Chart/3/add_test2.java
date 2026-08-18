// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCreateCopyWithNegativeValues() throws CloneNotSupportedException {
    TimeSeries s1 = new TimeSeries("S1");
    s1.add(new Year(2009), -50.0);
    s1.add(new Year(2010), -30.0);
    s1.add(new Year(2011), -40.0);
    
    TimeSeries s2 = s1.createCopy(0, 1);
    assertEquals(-50.0, s2.getMinY(), EPSILON);
    assertEquals(-30.0, s2.getMaxY(), EPSILON);
}