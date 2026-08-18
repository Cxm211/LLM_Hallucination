// org/jfree/chart/plot/junit/XYPlotTests.java
public void testGetDataRangeWithNullRenderer() {
        XYSeries series = new XYSeries("Series");
        series.add(1.0, 2.0);
        series.add(3.0, 4.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        XYPlot plot = new XYPlot();
        plot.setDataset(dataset);
        plot.setRenderer(null); // no renderer
        NumberAxis axis = new NumberAxis("Test Axis");
        plot.setRangeAxis(axis);
        // This should not throw NullPointerException
        Range range = plot.getDataRange(axis);
        // Expect range to be based on dataset y values
        assertNotNull(range);
        assertEquals(2.0, range.getLowerBound(), 0.001);
        assertEquals(4.0, range.getUpperBound(), 0.001);
    }
