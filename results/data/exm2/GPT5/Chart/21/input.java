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
        }
        
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
            else if (maxval > this.maximumRangeValue) {
                this.maximumRangeValue = maxval;
                this.maximumRangeValueRow = r;
                this.maximumRangeValueColumn = c;
            }
        
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
        
        this.rangeBounds = new Range(this.minimumRangeValue,
              this.maximumRangeValue);
        fireDatasetChanged();

    }

    private void updateBounds() {
        this.minimumRangeValue = Double.NaN;
        this.maximumRangeValue = Double.NaN;
    }

// trigger testcase
public void testGetRangeBounds() {
        DefaultBoxAndWhiskerCategoryDataset d1 
                = new DefaultBoxAndWhiskerCategoryDataset();
        d1.add(new BoxAndWhiskerItem(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 
                new ArrayList()), "R1", "C1");
        assertEquals(new Range(7.0, 8.0), d1.getRangeBounds(false));
        assertEquals(new Range(7.0, 8.0), d1.getRangeBounds(true));
        
        d1.add(new BoxAndWhiskerItem(1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 
                new ArrayList()), "R1", "C1");
        assertEquals(new Range(7.5, 8.5), d1.getRangeBounds(false));
        assertEquals(new Range(7.5, 8.5), d1.getRangeBounds(true));
        
        d1.add(new BoxAndWhiskerItem(2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 
                new ArrayList()), "R2", "C1");
        assertEquals(new Range(7.5, 9.5), d1.getRangeBounds(false));
        assertEquals(new Range(7.5, 9.5), d1.getRangeBounds(true));
        
        // this replaces the entry with the current minimum value, but the new
        // minimum value is now in a different item
        d1.add(new BoxAndWhiskerItem(1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 8.6, 9.6, 
                new ArrayList()), "R1", "C1");
        assertEquals(new Range(8.5, 9.6), d1.getRangeBounds(false));
        assertEquals(new Range(8.5, 9.6), d1.getRangeBounds(true));
    }
