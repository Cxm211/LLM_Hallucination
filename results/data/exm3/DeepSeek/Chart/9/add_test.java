// org/jfree/data/time/junit/TimeSeriesTests.java
public void testCreateCopyEmptyRangeInGap() {
        TimeSeries s = new TimeSeries(\"S\");
        s.add(new Day(1, 1, 2000), 1);
        s.add(new Day(1, 3, 2000), 2);
        s.add(new Day(1, 5, 2000), 3);
        boolean pass = true;
        try {
            s.createCopy(new Day(1, 2, 2000), new Day(15, 2, 2000));
        } catch (CloneNotSupportedException e) {
            pass = false;
        }
        assertTrue(pass);
    }
