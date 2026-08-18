// org/jfree/chart/plot/junit/MultiplePiePlotTests.java
public void testDatasetChangeListenerRegistration() {
    DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
    DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
    MultiplePiePlot plot = new MultiplePiePlot(dataset1);
    assertTrue(dataset1.hasListener(plot));
    assertFalse(dataset2.hasListener(plot));
    plot.setDataset(dataset2);
    assertFalse(dataset1.hasListener(plot));
    assertTrue(dataset2.hasListener(plot));
}