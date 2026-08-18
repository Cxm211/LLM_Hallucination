// org/jfree/chart/plot/junit/PiePlot3DTests.java
public void testGetMaximumExplodePercentWithNullDataset() {
        PiePlot3D plot = new PiePlot3D(null);
        double p = plot.getMaximumExplodePercent();
        assertEquals(0.0, p, 0.0000001);
    }