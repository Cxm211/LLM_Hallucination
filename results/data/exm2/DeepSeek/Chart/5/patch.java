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
        // if the series is sorted, the negative index is a result from
        // Collections.binarySearch() and tells us where to insert the
        // new item...otherwise it will be just -1 and we should just
        // append the value to the list...
        if (this.autoSort) {
            int insertIndex;
            if (index < 0) {
                insertIndex = -index - 1;
            }
            else {
                // index >= 0, so duplicate X is found, but since we are in else block,
                // allowDuplicateXValues must be true, so we need to insert after the last duplicate
                insertIndex = index;
                while (insertIndex < this.data.size() && 
                       ((XYDataItem) this.data.get(insertIndex)).getX().equals(x)) {
                    insertIndex++;
                }
            }
            this.data.add(insertIndex, new XYDataItem(x, y));
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