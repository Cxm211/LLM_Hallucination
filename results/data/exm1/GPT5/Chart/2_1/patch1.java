public static Range iterateRangeBounds(XYDataset dataset,
            boolean includeInterval) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        int seriesCount = dataset.getSeriesCount();

        // handle three cases by dataset type
        if (includeInterval && dataset instanceof IntervalXYDataset) {
            // handle special case of IntervalXYDataset
            IntervalXYDataset ixyd = (IntervalXYDataset) dataset;
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    double y = dataset.getYValue(series, item);
                    double lvalue = ixyd.getStartYValue(series, item);
                    double uvalue = ixyd.getEndYValue(series, item);
                    // consider startY and fallback/include y for minimum
                    double minCandidate = Double.NaN;
                    if (!Double.isNaN(lvalue)) {
                        minCandidate = lvalue;
                    }
                    if (!Double.isNaN(y)) {
                        minCandidate = Double.isNaN(minCandidate) ? y : Math.min(minCandidate, y);
                    }
                    if (!Double.isNaN(minCandidate)) {
                        minimum = Math.min(minimum, minCandidate);
                    }
                    // consider endY and fallback/include y for maximum
                    double maxCandidate = Double.NaN;
                    if (!Double.isNaN(uvalue)) {
                        maxCandidate = uvalue;
                    }
                    if (!Double.isNaN(y)) {
                        maxCandidate = Double.isNaN(maxCandidate) ? y : Math.max(maxCandidate, y);
                    }
                    if (!Double.isNaN(maxCandidate)) {
                        maximum = Math.max(maximum, maxCandidate);
                    }
                }
            }
        }
        else if (includeInterval && dataset instanceof OHLCDataset) {
            // handle special case of OHLCDataset
            OHLCDataset ohlc = (OHLCDataset) dataset;
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    double lvalue = ohlc.getLowValue(series, item);
                    double uvalue = ohlc.getHighValue(series, item);
                    if (!Double.isNaN(lvalue)) {
                        minimum = Math.min(minimum, lvalue);
                    }
                    if (!Double.isNaN(uvalue)) {
                        maximum = Math.max(maximum, uvalue);
                    }
                }
            }
        }
        else {
            // standard case - plain XYDataset
            for (int series = 0; series < seriesCount; series++) {
                int itemCount = dataset.getItemCount(series);
                for (int item = 0; item < itemCount; item++) {
                    double value = dataset.getYValue(series, item);
                    if (!Double.isNaN(value)) {
                        minimum = Math.min(minimum, value);
                        maximum = Math.max(maximum, value);
                    }
                }
            }
        }
        if (minimum == Double.POSITIVE_INFINITY) {
            return null;
        }
        else {
            return new Range(minimum, maximum);
        }
    }