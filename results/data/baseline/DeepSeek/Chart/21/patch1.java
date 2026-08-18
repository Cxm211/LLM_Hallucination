private void updateBounds() {
        // Recompute minimum and maximum based on all items
        this.minimumRangeValue = Double.NaN;
        this.maximumRangeValue = Double.NaN;
        int rowCount = this.data.getRowCount();
        int columnCount = this.data.getColumnCount();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getValue(r, c);
                if (item != null) {
                    Number min = item.getMinOutlier();
                    if (min != null) {
                        double minv = min.doubleValue();
                        if (Double.isNaN(this.minimumRangeValue) || minv < this.minimumRangeValue) {
                            this.minimumRangeValue = minv;
                            this.minimumRangeValueRow = r;
                            this.minimumRangeValueColumn = c;
                        }
                    }
                    Number max = item.getMaxOutlier();
                    if (max != null) {
                        double maxv = max.doubleValue();
                        if (Double.isNaN(this.maximumRangeValue) || maxv > this.maximumRangeValue) {
                            this.maximumRangeValue = maxv;
                            this.maximumRangeValueRow = r;
                            this.maximumRangeValueColumn = c;
                        }
                    }
                }
            }
        }
    }