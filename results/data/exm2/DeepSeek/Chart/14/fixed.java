// ===== FIXED org.jfree.chart.plot.CategoryPlot :: removeDomainMarker(int, Marker, Layer, boolean) [lines 2155-2174] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-14-fixed/source/org/jfree/chart/plot/CategoryPlot.java =====
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
        if (markers == null) {
            return false;
        }
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }

// ===== FIXED org.jfree.chart.plot.CategoryPlot :: removeRangeMarker(int, Marker, Layer, boolean) [lines 2437-2459] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-14-fixed/source/org/jfree/chart/plot/CategoryPlot.java =====
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
        if (markers == null) {
            return false;
        }
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }

// ===== FIXED org.jfree.chart.plot.XYPlot :: removeDomainMarker(int, Marker, Layer, boolean) [lines 2282-2301] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-14-fixed/source/org/jfree/chart/plot/XYPlot.java =====
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
        if (markers == null) {
            return false;
        }
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }

// ===== FIXED org.jfree.chart.plot.XYPlot :: removeRangeMarker(int, Marker, Layer, boolean) [lines 2518-2540] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-14-fixed/source/org/jfree/chart/plot/XYPlot.java =====
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
        if (markers == null) {
            return false;
        }
        boolean removed = markers.remove(marker);
        if (removed && notify) {
            fireChangeEvent();
        }
        return removed;
    }
