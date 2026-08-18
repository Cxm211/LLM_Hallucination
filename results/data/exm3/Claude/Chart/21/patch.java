public void add(BoxAndWhiskerItem item, Comparable rowKey, 
            Comparable columnKey) {

        this.data.addObject(item, rowKey, columnKey);
        
        // update cached min and max values
        int r = this.data.getRowIndex(rowKey);
        int c = this.data.getColumnIndex(columnKey);
        if ((this.maximumRangeValueRow == r && this.maximumRangeValueColumn 
                == c) || (this.minimumRangeValueRow == r 
                && this.minimumRangeValueColumn == c))  {
            updateBounds();
            // Need to recalculate bounds from all items
            for (int i = 0; i < this.data.getRowCount(); i++) {
                for (int j = 0; j < this.data.getColumnCount(); j++) {
                    BoxAndWhiskerItem existingItem = (BoxAndWhiskerItem) this.data.getObject(i, j);
                    if (existingItem != null) {
                        double minval = Double.NaN;
                        if (existingItem.getMinOutlier() != null) {
                            minval = existingItem.getMinOutlier().doubleValue();
                        }
                        double maxval = Double.NaN;
                        if (existingItem.getMaxOutlier() != null) {
                            maxval = existingItem.getMaxOutlier().doubleValue();
                        }
                        
                        if (Double.isNaN(this.maximumRangeValue)) {
                            this.maximumRangeValue = maxval;
                            this.maximumRangeValueRow = i;
                            this.maximumRangeValueColumn = j;
                        }
                        else if (!Double.isNaN(maxval) && maxval > this.maximumRangeValue) {
                            this.maximumRangeValue = maxval;
                            this.maximumRangeValueRow = i;
                            this.maximumRangeValueColumn = j;
                        }
                        
                        if (Double.isNaN(this.minimumRangeValue)) {
                            this.minimumRangeValue = minval;
                            this.minimumRangeValueRow = i;
                            this.minimumRangeValueColumn = j;
                        }
                        else if (!Double.isNaN(minval) && minval < this.minimumRangeValue) {
                            this.minimumRangeValue = minval;
                            this.minimumRangeValueRow = i;
                            this.minimumRangeValueColumn = j;
                        }
                    }
                }
            }
        }
        else {
            double minval = Double.NaN;
            if (item.getMinOutlier() != null) {
                minval = item.getMinOutlier().doubleValue();
            }
            double maxval = Double.NaN;
            if (item.getMaxOutlier() != null) {
                maxval = item.getMaxOutlier().doubleValue();
            }
        
            if (Double.isNaN(this.maximumRangeValue)) {
                this.maximumRangeValue = maxval;
                this.maximumRangeValueRow = r;
                this.maximumRangeValueColumn = c;
            }
            else if (!Double.isNaN(maxval) && maxval > this.maximumRangeValue) {
                this.maximumRangeValue = maxval;
                this.maximumRangeValueRow = r;
                this.maximumRangeValueColumn = c;
            }
        
            if (Double.isNaN(this.minimumRangeValue)) {
                this.minimumRangeValue = minval;
                this.minimumRangeValueRow = r;
                this.minimumRangeValueColumn = c;
            }
            else if (!Double.isNaN(minval) && minval < this.minimumRangeValue) {
                this.minimumRangeValue = minval;
                this.minimumRangeValueRow = r;
                this.minimumRangeValueColumn = c;
            }
        }
        
        this.rangeBounds = new Range(this.minimumRangeValue,
              this.maximumRangeValue);
        fireDatasetChanged();

    }