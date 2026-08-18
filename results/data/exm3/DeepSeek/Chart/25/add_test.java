// org/jfree/chart/renderer/category/junit/StatisticalBarRendererTests.java
public void testDrawWithNullDeviationMultipleSeries() {
    boolean success = false;
    try {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(1.0, 2.0, "S1", "C1");
        dataset.add(3.0, null, "S2", "C1");
        CategoryPlot plot = new CategoryPlot(dataset, 
                new CategoryAxis("Category"), new NumberAxis("Value"), 
                new StatisticalBarRenderer());
        JFreeChart chart = new JFreeChart(plot);
        chart.createBufferedImage(300, 200, null);
        success = true;
    } catch (NullPointerException e) {
        e.printStackTrace();
        success = false;
    }
    assertTrue(success);
}
