// org/jfree/chart/plot/junit/XYPlotTests.java
public void testGetDataRangeWithBothDomainAndRangeAxis() {
        // Create an axis that will be used as both domain and range
        NumberAxis axis = new NumberAxis("Shared Axis");
        
        // Create two datasets
        XYSeries series1 = new XYSeries("Domain Dataset");
        series1.add(10.0, 0.0); // x values will be used for domain
        series1.add(20.0, 0.0);
        XYSeriesCollection dataset1 = new XYSeriesCollection(series1);
        
        XYSeries series2 = new XYSeries("Range Dataset");
        series2.add(0.0, 30.0); // y values will be used for range
        series2.add(0.0, 40.0);
        XYSeriesCollection dataset2 = new XYSeriesCollection(series2);
        
        XYPlot plot = new XYPlot();
        // Map dataset1 to domain axis 0
        plot.setDataset(0, dataset1);
        plot.mapDatasetToDomainAxis(0, 0);
        // Map dataset2 to range axis 0
        plot.setDataset(1, dataset2);
        plot.mapDatasetToRangeAxis(1, 0);
        // Set the same axis as both domain and range axis at index 0
        plot.setDomainAxis(0, axis);
        plot.setRangeAxis(0, axis);
        
        // Now getDataRange for that axis should combine domain values (10,20) and range values (30,40)
        Range range = plot.getDataRange(axis);
        assertNotNull(range);
        // The range should be from 10 to 40
        assertEquals(10.0, range.getLowerBound(), 0.001);
        assertEquals(40.0, range.getUpperBound(), 0.001);
    }
