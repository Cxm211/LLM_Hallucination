// org/jfree/chart/plot/junit/XYPlotTests.java::testDrawRangeGridlines
public void testGetDataRangeWithNullRenderer() {
        // dataset with actual data
        XYSeries series = new XYSeries("S1");
        series.add(1.0, 2.0);
        series.add(3.0, 4.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Title", "X", "Y", dataset, PlotOrientation.VERTICAL,
                false, false, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        // set renderer to null to force use of DatasetUtilities in getDataRange
        plot.setRenderer(null);
        Range r = plot.getDataRange(plot.getDomainAxis());
        assertNotNull(r);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(3.0, r.getUpperBound(), EPSILON);
    }