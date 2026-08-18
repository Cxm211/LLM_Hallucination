// org/jfree/chart/plot/junit/XYPlotTests.java
public void testRemoveRangeMarkerNull() {
    XYPlot plot = new XYPlot();
    try {
        plot.removeRangeMarker(null);
        fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
        // expected
    }
}