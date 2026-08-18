public static Range iterateDomainBounds(XYDataset dataset,
                                            boolean includeInterval) {
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        int seriesCount = dataset.getSeriesCount();
        double lvalue;
        double uvalue;
        if (includeInterval && dataset instanceof IntervalXYDataset) {
            IntervalXYDataset intervalXYData = (IntervalXYDataset) dataset;
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    lvalue = intervalXYData.getStartXValue(series, item);
                    uvalue = intervalXYData.getEndXValue(series, item);
                    double x = dataset.getXValue(series, item);
                    double lo = Double.POSITIVE_INFINITY;
                    double hi = Double.NEGATIVE_INFINITY;
                    if (!Double.isNaN(lvalue)) {
                        lo = Math.min(lo, lvalue);
                        hi = Math.max(hi, lvalue);
                    }
                    if (!Double.isNaN(uvalue)) {
                        lo = Math.min(lo, uvalue);
                        hi = Math.max(hi, uvalue);
                    }
                    if (!Double.isNaN(x)) {
                        lo = Math.min(lo, x);
                        hi = Math.max(hi, x);
                    }
                    if (lo <= hi) {
                        minimum = Math.min(minimum, lo);
                        maximum = Math.max(maximum, hi);
                    }
                }
            }
        }
        else {
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    lvalue = dataset.getXValue(series, item);
                    uvalue = lvalue;
                    if (!Double.isNaN(lvalue)) {
                        minimum = Math.min(minimum, lvalue);
                        maximum = Math.max(maximum, uvalue);
                    }
                }
            }
        }
        if (minimum > maximum) {
            return null;
        }
        else {
            return new Range(minimum, maximum);
        }
    }