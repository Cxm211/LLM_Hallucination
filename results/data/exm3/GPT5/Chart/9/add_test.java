// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCreateCopyEmptyRangeWithinGap() {
        TimeSeries s = new TimeSeries("S");
        s.add(new Day(1, 3, 2020), 1);
        s.add(new Day(1, 6, 2020), 2);
        try {
            TimeSeries s2 = s.createCopy(new Day(1, 4, 2020), new Day(1, 5, 2020));
            assertEquals(0, s2.getItemCount());
        }
        catch (CloneNotSupportedException e) {
            fail("CloneNotSupportedException thrown");
        }
    }