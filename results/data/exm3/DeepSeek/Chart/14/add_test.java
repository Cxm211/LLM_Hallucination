// org/jfree/chart/plot/junit/CategoryPlotTests.java
public void testRemoveDomainMarkerNullMarker() {
        CategoryPlot plot = new CategoryPlot();
        try {
            plot.removeDomainMarker(0, null, Layer.FOREGROUND, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
