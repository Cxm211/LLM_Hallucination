// buggy function
    public boolean removeDomainMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        ArrayList markers;
        if (layer == Layer.FOREGROUND) {
            markers = (ArrayList) this.foregroundDomainMarkers.get(new Integer(
                    index));
        }
        else {
            markers = (ArrayList) this.backgroundDomainMarkers.get(new Integer(
                    index));
        }
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }

    public boolean removeRangeMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        if (marker == null) {
            throw new IllegalArgumentException("Null 'marker' argument.");
        }
        ArrayList markers;
        if (layer == Layer.FOREGROUND) {
            markers = (ArrayList) this.foregroundRangeMarkers.get(new Integer(
                    index));
        }
        else {
            markers = (ArrayList) this.backgroundRangeMarkers.get(new Integer(
                    index));
        }
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }

    public boolean removeDomainMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        ArrayList markers;
        if (layer == Layer.FOREGROUND) {
            markers = (ArrayList) this.foregroundDomainMarkers.get(new Integer(
                    index));
        }
        else {
            markers = (ArrayList) this.backgroundDomainMarkers.get(new Integer(
                    index));
        }
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }

    public boolean removeRangeMarker(int index, Marker marker, Layer layer,
    		boolean notify) {
        if (marker == null) {
            throw new IllegalArgumentException("Null 'marker' argument.");
        }
        ArrayList markers;
        if (layer == Layer.FOREGROUND) {
            markers = (ArrayList) this.foregroundRangeMarkers.get(new Integer(
                    index));
        }
        else {
            markers = (ArrayList) this.backgroundRangeMarkers.get(new Integer(
                    index));
        }
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }

// trigger testcase
// org/jfree/chart/plot/junit/CategoryPlotTests.java::testRemoveDomainMarker
public void testRemoveDomainMarker() {
    	CategoryPlot plot = new CategoryPlot();
    	assertFalse(plot.removeDomainMarker(new CategoryMarker("Category 1")));
    }

// org/jfree/chart/plot/junit/CategoryPlotTests.java::testRemoveRangeMarker
public void testRemoveRangeMarker() {
    	CategoryPlot plot = new CategoryPlot();
    	assertFalse(plot.removeRangeMarker(new ValueMarker(0.5)));
    }

// org/jfree/chart/plot/junit/XYPlotTests.java::testRemoveDomainMarker
public void testRemoveDomainMarker() {
    	XYPlot plot = new XYPlot();
    	assertFalse(plot.removeDomainMarker(new ValueMarker(0.5)));
    }

// org/jfree/chart/plot/junit/XYPlotTests.java::testRemoveRangeMarker
public void testRemoveRangeMarker() {
    	XYPlot plot = new XYPlot();
    	assertFalse(plot.removeRangeMarker(new ValueMarker(0.5)));
    }
