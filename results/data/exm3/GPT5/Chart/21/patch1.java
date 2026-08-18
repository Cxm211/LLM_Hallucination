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
                if (obj instanceof BoxAndWhiskerItem) {
                    BoxAndWhiskerItem i = (BoxAndWhiskerItem) obj;
                    Number minOut = i.getMinOutlier();
                    Number maxOut = i.getMaxOutlier();
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
        if (!Double.isNaN(this.minimumRangeValue) && !Double.isNaN(this.maximumRangeValue)) {
            this.rangeBounds = new Range(this.minimumRangeValue, this.maximumRangeValue);
        } else {
            this.rangeBounds = null;
        }
    }