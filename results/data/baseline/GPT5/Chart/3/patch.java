public TimeSeries createCopy(int start, int end)
        throws CloneNotSupportedException {
    if (start < 0) {
        throw new IllegalArgumentException("Requires start >= 0.");
    }
    if (end < start) {
        throw new IllegalArgumentException("Requires start <= end.");
    }
    TimeSeries copy = (TimeSeries) super.clone();
    copy.data = new java.util.ArrayList();
    int itemCount = (this.data != null) ? this.data.size() : 0;
    if (itemCount > 0) {
        int s = Math.max(start, 0);
        int e = Math.min(end, itemCount - 1);
        for (int index = s; index <= e; index++) {
            TimeSeriesDataItem item = (TimeSeriesDataItem) this.data.get(index);
            TimeSeriesDataItem clone = (TimeSeriesDataItem) item.clone();
            try {
                copy.add(clone);
            }
            catch (SeriesException e) {
                e.printStackTrace();
            }
        }
    }
    return copy;
}
