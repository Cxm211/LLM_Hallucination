public XYDataItem addOrUpdate(Number x, Number y) {
        if (x == null) {
            throw new IllegalArgumentException("Null 'x' argument.");
        }

        // if we get to here, we know that duplicate X values are not permitted
        XYDataItem overwritten = null;
        int index = indexOf(x);
        if (index >= 0 && !this.allowDuplicateXValues) {
            XYDataItem existing = (XYDataItem) this.data.get(index);
            try {
                overwritten = (XYDataItem) existing.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new SeriesException("Couldn't clone XYDataItem!");
            }
            existing.setY(y);
        }
        else {
            if (this.autoSort) {
                XYDataItem newItem = new XYDataItem(x, y);
                int r = java.util.Collections.binarySearch(this.data, newItem);
                int insertionPoint;
                if (r >= 0) {
                    int j = r;
                    Number xx = x;
                    while (j + 1 < this.data.size()
                            && ((XYDataItem) this.data.get(j + 1)).getX().equals(xx)) {
                        j++;
                    }
                    insertionPoint = j + 1;
                } else {
                    insertionPoint = -r - 1;
                }
                this.data.add(insertionPoint, newItem);
            }
            else {
                this.data.add(new XYDataItem(x, y));
            }
            // check if this addition will exceed the maximum item count...
            if (getItemCount() > this.maximumItemCount) {
                this.data.remove(0);
            }
        }
        fireSeriesChanged();
        return overwritten;
    }