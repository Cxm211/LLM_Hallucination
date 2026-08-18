// org/jfree/chart/plot/junit/XYPlotTests.java::testRemoveDomainMarkerWithParams
public void testRemoveDomainMarkerWithParams() {
    XYPlot plot = new XYPlot();
    try {
        plot.removeDomainMarker(0, null, Layer.FOREGROUND, true);
        fail("Expected IllegalArgumentException for null marker.");
    } catch (IllegalArgumentException e) {
        // expected
    }
    assertFalse(plot.removeDomainMarker(0, new ValueMarker(1.0), Layer.FOREGROUND, true));
    assertFalse(plot.removeRangeMarker(0, new ValueMarker(0.5), Layer.BACKGROUND, true));
}