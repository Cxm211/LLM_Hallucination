// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCreateCopyEndTooBig() throws CloneNotSupportedException {
        TimeSeries s1 = new TimeSeries("S1");
        s1.add(new Year(2009), 100.0);
        s1.add(new Year(2010), 101.0);
        s1.add(new Year(2011), 102.0);

        TimeSeries s2 = s1.createCopy(1, 99);
        assertEquals(2, s2.getItemCount());
        assertEquals(101.0, s2.getMinY(), EPSILON);
        assertEquals(102.0, s2.getMaxY(), EPSILON);
    }