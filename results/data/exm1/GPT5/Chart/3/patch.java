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
        int count = this.data.size();
        if (count == 0) {
            return copy;
        }
        int from = Math.max(start, 0);
        int to = Math.min(end, count - 1);
        if (from > to) {
            return copy;
        }
        for (int index = from; index <= to; index++) {
            TimeSeriesDataItem item
                    = (TimeSeriesDataItem) this.data.get(index);
            TimeSeriesDataItem clone = (TimeSeriesDataItem) item.clone();
            try {
                copy.add(clone);
            }
            catch (SeriesException e) {
                e.printStackTrace();
            }
        }
        return copy;
    }