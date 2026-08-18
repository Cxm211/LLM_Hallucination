public boolean removeDomainMarker(int index, Marker marker, Layer layer, boolean notify) {
    if (marker == null) {
        throw new IllegalArgumentException("Null 'marker' argument.");
    }
    ArrayList markers;
    if (layer == Layer.FOREGROUND) {
        markers = (ArrayList) this.foregroundDomainMarkers.get(new Integer(index));
    } else {
        markers = (ArrayList) this.backgroundDomainMarkers.get(new Integer(index));
    }
    boolean removed = false;
    if (markers != null) {
        removed = markers.remove(marker);
        if (markers.isEmpty()) {
            if (layer == Layer.FOREGROUND) {
                this.foregroundDomainMarkers.remove(new Integer(index));
            } else {
                this.backgroundDomainMarkers.remove(new Integer(index));
            }
        }
    }
    if (removed && notify) {
        fireChangeEvent();
    }
    return removed;
}