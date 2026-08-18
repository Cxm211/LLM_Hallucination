// org/jfree/chart/renderer/category/junit/StatisticalBarRendererTests.java
public void testDrawWithNullDeviationAndPositiveClip() {
    boolean success = false;
    try {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(1.0, 2.0, "S1", "C1");
        dataset.add(3.0, null, "S1", "C2");
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setLowerClip(1.0);
        renderer.setUpperClip(5.0);
        CategoryPlot plot = new CategoryPlot(dataset, 
                new CategoryAxis("Category"), new NumberAxis("Value"), 
                renderer);
        JFreeChart chart = new JFreeChart(plot);
        chart.createBufferedImage(300, 200, null);
        success = true;
    } catch (NullPointerException e) {
        e.printStackTrace();
        success = false;
    }
    assertTrue(success);
}
