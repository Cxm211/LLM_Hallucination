// org/jfree/chart/plot/junit/PiePlot3DTests.java
public void testGetMaximumExplodePercentWithNullDataset() {
    PiePlot3D plot = new PiePlot3D(null);
    double maxExplode = plot.getMaximumExplodePercent();
    assertEquals(0.0, maxExplode, 0.0001);
}