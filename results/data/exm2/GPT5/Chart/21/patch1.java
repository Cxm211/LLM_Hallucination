private void updateBounds() {
        // recompute cached min and max values across the dataset
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
                Number minOut = item.getMinOutlier();
                Number maxOut = item.getMaxOutlier();
                if (minOut != null) {
                    double v = minOut.doubleValue();
                    if (Double.isNaN(this.minimumRangeValue) || v < this.minimumRangeValue) {
                        this.minimumRangeValue = v;
                        this.minimumRangeValueRow = r;
                        this.minimumRangeValueColumn = c;
                    }
                }
                if (maxOut != null) {
                    double v = maxOut.doubleValue();
                    if (Double.isNaN(this.maximumRangeValue) || v > this.maximumRangeValue) {
                        this.maximumRangeValue = v;
                        this.maximumRangeValueRow = r;
                        this.maximumRangeValueColumn = c;
                    }
                }
            }
        }
    }