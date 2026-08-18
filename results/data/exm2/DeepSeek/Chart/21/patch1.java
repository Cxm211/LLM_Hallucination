    private void updateBounds() {
        // recompute min and max from all items
        this.minimumRangeValue = Double.NaN;
        this.maximumRangeValue = Double.NaN;
        for (int row = 0; row < this.data.getRowCount(); row++) {
            for (int col = 0; col < this.data.getColumnCount(); col++) {
                BoxAndWhiskerItem it = (BoxAndWhiskerItem) this.data.getObject(row, col);
                if (it != null) {
                    double minOutlier = Double.NaN;
                    Number minOutlierNum = it.getMinOutlier();
                    if (minOutlierNum != null) {
                        minOutlier = minOutlierNum.doubleValue();
                    }
                    double maxOutlier = Double.NaN;
                    Number maxOutlierNum = it.getMaxOutlier();
                    if (maxOutlierNum != null) {
                        maxOutlier = maxOutlierNum.doubleValue();
                    }
                    if (!Double.isNaN(minOutlier)) {
                        if (Double.isNaN(this.minimumRangeValue) || minOutlier < this.minimumRangeValue) {
                            this.minimumRangeValue = minOutlier;
                            this.minimumRangeValueRow = row;
                            this.minimumRangeValueColumn = col;
                        }
                    }
                    if (!Double.isNaN(maxOutlier)) {
                        if (Double.isNaN(this.maximumRangeValue) || maxOutlier > this.maximumRangeValue) {
                            this.maximumRangeValue = maxOutlier;
                            this.maximumRangeValueRow = row;
                            this.maximumRangeValueColumn = col;
                        }
                    }
                }
            }
        }
    }