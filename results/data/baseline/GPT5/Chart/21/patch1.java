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
                Object obj = this.data.getObject(r, c);
                if (!(obj instanceof BoxAndWhiskerItem)) {
                    continue;
                }
                BoxAndWhiskerItem i = (BoxAndWhiskerItem) obj;
                Number minN = i.getMinOutlier();
                Number maxN = i.getMaxOutlier();
                if (minN != null) {
                    double v = minN.doubleValue();
                    if (Double.isNaN(this.minimumRangeValue) || v < this.minimumRangeValue) {
                        this.minimumRangeValue = v;
                        this.minimumRangeValueRow = r;
                        this.minimumRangeValueColumn = c;
                    }
                }
                if (maxN != null) {
                    double v = maxN.doubleValue();
                    if (Double.isNaN(this.maximumRangeValue) || v > this.maximumRangeValue) {
                        this.maximumRangeValue = v;
                        this.maximumRangeValueRow = r;
                        this.maximumRangeValueColumn = c;
                    }
                }
            }
        }
    }