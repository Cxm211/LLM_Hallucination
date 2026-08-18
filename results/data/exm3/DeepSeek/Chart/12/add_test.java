// org/jfree/chart/plot/junit/MultiplePiePlotTests.java
public void testConstructorMultiplePlots() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MultiplePiePlot plot1 = new MultiplePiePlot(dataset);
        MultiplePiePlot plot2 = new MultiplePiePlot(dataset);
        assertTrue(dataset.hasListener(plot1));
        assertTrue(dataset.hasListener(plot2));
    }
