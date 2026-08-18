// org/jfree/chart/plot/junit/XYPlotTests.java
public void testRemoveRangeMarkerBackgroundLayer() {
        XYPlot plot = new XYPlot();
        boolean removed = plot.removeRangeMarker(1, new ValueMarker(0.5), Layer.BACKGROUND, true);
        assertFalse(removed);
    }
