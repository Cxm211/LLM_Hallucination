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
                    double x = dataset.getXValue(series, item);
                    lvalue = intervalXYData.getStartXValue(series, item);
                    uvalue = intervalXYData.getEndXValue(series, item);
                    // for minimum, consider startX and fall back/include x
                    double minCandidate = Double.NaN;
                    if (!Double.isNaN(lvalue)) {
                        minCandidate = lvalue;
                    }
                    if (!Double.isNaN(x)) {
                        minCandidate = Double.isNaN(minCandidate) ? x : Math.min(minCandidate, x);
                    }
                    if (!Double.isNaN(minCandidate)) {
                        minimum = Math.min(minimum, minCandidate);
                    }
                    // for maximum, consider endX and fall back/include x
                    double maxCandidate = Double.NaN;
                    if (!Double.isNaN(uvalue)) {
                        maxCandidate = uvalue;
                    }
                    if (!Double.isNaN(x)) {
                        maxCandidate = Double.isNaN(maxCandidate) ? x : Math.max(maxCandidate, x);
                    }
                    if (!Double.isNaN(maxCandidate)) {
                        maximum = Math.max(maximum, maxCandidate);
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