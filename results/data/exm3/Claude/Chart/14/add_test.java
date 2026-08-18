// org/jfree/chart/plot/junit/CategoryPlotTests.java
public void testRemoveDomainMarkerNull() {
    CategoryPlot plot = new CategoryPlot();
    try {
        plot.removeDomainMarker(null);
        fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
        // expected
    }
}