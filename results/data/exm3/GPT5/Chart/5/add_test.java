// org/jfree/data/xy/junit/XYSeriesTests.java
public void testAddDuplicateMaintainsOrderWithAutoSort() {
        XYSeries series = new XYSeries("Series", true, true);
        series.addOrUpdate(1.0, 1.0);
        series.addOrUpdate(1.0, 2.0);
        series.addOrUpdate(1.0, 3.0);
        assertEquals(3, series.getItemCount());
        assertEquals(new Double(1.0), series.getY(0));
        assertEquals(new Double(2.0), series.getY(1));
        assertEquals(new Double(3.0), series.getY(2));
    }