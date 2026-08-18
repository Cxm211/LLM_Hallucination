    private void updateBounds() {
        this.minimumRangeValue = Double.NaN;
        this.maximumRangeValue = Double.NaN;
        this.minimumRangeValueRow = -1;
        this.minimumRangeValueColumn = -1;
        this.maximumRangeValueRow = -1;
        this.maximumRangeValueColumn = -1;

        int rowCount = this.data.getRowCount();
        int colCount = this.data.getColumnCount();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(r, c);
                if (item == null) {
                    continue;
                }
                Number minOutlier = item.getMinOutlier();
                Number maxOutlier = item.getMaxOutlier();
                double minVal = Double.NaN;
                double maxVal = Double.NaN;
                if (minOutlier != null) {
                    minVal = minOutlier.doubleValue();
                }
                if (maxOutlier != null) {
                    maxVal = maxOutlier.doubleValue();
                }
                if (!Double.isNaN(minVal)) {
                    if (Double.isNaN(this.minimumRangeValue) || minVal < this.minimumRangeValue) {
                        this.minimumRangeValue = minVal;
                        this.minimumRangeValueRow = r;
                        this.minimumRangeValueColumn = c;
                    }
                }
                if (!Double.isNaN(maxVal)) {
                    if (Double.isNaN(this.maximumRangeValue) || maxVal > this.maximumRangeValue) {
                        this.maximumRangeValue = maxVal;
                        this.maximumRangeValueRow = r;
                        this.maximumRangeValueColumn = c;
                    }
                }
            }
        }
    }