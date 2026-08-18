public boolean removeRangeMarker(int index, Marker marker, Layer layer, boolean notify) {
    if (marker == null) {
        throw new IllegalArgumentException("Null 'marker' argument.");
    }
    ArrayList markers;
    if (layer == Layer.FOREGROUND) {
        markers = (ArrayList) this.foregroundRangeMarkers.get(new Integer(index));
    } else {
        markers = (ArrayList) this.backgroundRangeMarkers.get(new Integer(index));
    }
    boolean removed = false;
    if (markers != null) {
        removed = markers.remove(marker);
        if (markers.isEmpty()) {
            if (layer == Layer.FOREGROUND) {
                this.foregroundRangeMarkers.remove(new Integer(index));
            } else {
                this.backgroundRangeMarkers.remove(new Integer(index));
            }
        }
    }
    if (removed && notify) {
        fireChangeEvent();
    }
    return removed;
}