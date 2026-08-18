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
        // reset cached bounds so they are recalculated for the copy
        copy.minY = Double.NaN;
        copy.maxY = Double.NaN;

        if (this.data.size() > 0) {
            int upper = Math.min(end, this.data.size() - 1);
            if (start <= upper) {
                for (int index = start; index <= upper; index++) {
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
            }
        }
        return copy;
    }