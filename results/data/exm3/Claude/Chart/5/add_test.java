// org/jfree/data/xy/junit/XYSeriesTests.java
public void testAddOrUpdateWithDuplicatesNotAllowedAndAutoSort() {
        XYSeries series = new XYSeries("Series", true, false);
        series.addOrUpdate(2.0, 1.0);
        series.addOrUpdate(1.0, 2.0);
        series.addOrUpdate(3.0, 3.0);
        XYDataItem result = series.addOrUpdate(2.0, 5.0);
        assertEquals(3, series.getItemCount());
        assertEquals(new Double(5.0), series.getY(1));
        assertNotNull(result);
        assertEquals(new Double(1.0), result.getY());
    }