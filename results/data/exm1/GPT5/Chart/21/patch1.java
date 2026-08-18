private void updateBounds() {
        double min = Double.NaN;
        double max = Double.NaN;
        int minRow = -1;
        int minCol = -1;
        int maxRow = -1;
        int maxCol = -1;

        int rowCount = this.data.getRowCount();
        int colCount = this.data.getColumnCount();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                Object obj = this.data.getObject(i, j);
                if (obj instanceof BoxAndWhiskerItem) {
                    BoxAndWhiskerItem it = (BoxAndWhiskerItem) obj;
                    Number minOut = it.getMinOutlier();
                    Number maxOut = it.getMaxOutlier();
                    if (minOut != null) {
                        double v = minOut.doubleValue();
                        if (Double.isNaN(min) || v < min) {
                            min = v;
                            minRow = i;
                            minCol = j;
                        }
                    }
                    if (maxOut != null) {
                        double v = maxOut.doubleValue();
                        if (Double.isNaN(max) || v > max) {
                            max = v;
                            maxRow = i;
                            maxCol = j;
                        }
                    }
                }
            }
        }

        this.minimumRangeValue = min;
        this.maximumRangeValue = max;
        this.minimumRangeValueRow = minRow;
        this.minimumRangeValueColumn = minCol;
        this.maximumRangeValueRow = maxRow;
        this.maximumRangeValueColumn = maxCol;
    }