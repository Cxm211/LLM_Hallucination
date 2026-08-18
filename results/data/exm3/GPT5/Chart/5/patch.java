public XYDataItem addOrUpdate(Number x, Number y) {
        if (x == null) {
            throw new IllegalArgumentException("Null 'x' argument.");
        }

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
            XYDataItem newItem = new XYDataItem(x, y);
            if (this.autoSort) {
                int insertionIndex;
                if (index < 0) {
                    // index provides the insertion point when autoSort is true
                    insertionIndex = -index - 1;
                } else {
                    // duplicate X-values allowed: insert after the last item with same X
                    int i = index;
                    Comparable xComp = (Comparable) newItem.getX();
                    while (i < this.data.size()) {
                        XYDataItem di = (XYDataItem) this.data.get(i);
                        int cmp = ((Comparable) di.getX()).compareTo(xComp);
                        if (cmp == 0) {
                            i++;
                        } else {
                            break;
                        }
                    }
                    insertionIndex = i;
                }
                this.data.add(insertionIndex, new XYDataItem(x, y));
            }
            else {
                this.data.add(newItem);
            }
            if (getItemCount() > this.maximumItemCount) {
                this.data.remove(0);
            }
        }
        fireSeriesChanged();
        return overwritten;
    }