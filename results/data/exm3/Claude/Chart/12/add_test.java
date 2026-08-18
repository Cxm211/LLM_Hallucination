// org/jfree/chart/plot/junit/MultiplePiePlotTests.java
public void testConstructorWithNullDataset() {
    MultiplePiePlot plot = new MultiplePiePlot(null);
    assertNull(plot.getDataset());
}