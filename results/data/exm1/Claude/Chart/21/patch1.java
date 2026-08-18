private void updateBounds() {
        this.minimumRangeValue = Double.NaN;
        this.maximumRangeValue = Double.NaN;
        this.minimumRangeValueRow = -1;
        this.minimumRangeValueColumn = -1;
        this.maximumRangeValueRow = -1;
        this.maximumRangeValueColumn = -1;
        
        int rowCount = this.data.getRowCount();
        int columnCount = this.data.getColumnCount();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(r, c);
                if (item != null) {
                    Number minOutlier = item.getMinOutlier();
                    if (minOutlier != null) {
                        double minval = minOutlier.doubleValue();
                        if (Double.isNaN(this.minimumRangeValue)) {
                            this.minimumRangeValue = minval;
                            this.minimumRangeValueRow = r;
                            this.minimumRangeValueColumn = c;
                        }
                        else if (minval < this.minimumRangeValue) {
                            this.minimumRangeValue = minval;
                            this.minimumRangeValueRow = r;
                            this.minimumRangeValueColumn = c;
                        }
                    }
                    Number maxOutlier = item.getMaxOutlier();
                    if (maxOutlier != null) {
                        double maxval = maxOutlier.doubleValue();
                        if (Double.isNaN(this.maximumRangeValue)) {
                            this.maximumRangeValue = maxval;
                            this.maximumRangeValueRow = r;
                            this.maximumRangeValueColumn = c;
                        }
                        else if (maxval > this.maximumRangeValue) {
                            this.maximumRangeValue = maxval;
                            this.maximumRangeValueRow = r;
                            this.maximumRangeValueColumn = c;
                        }
                    }
                }
            }
        }
    }