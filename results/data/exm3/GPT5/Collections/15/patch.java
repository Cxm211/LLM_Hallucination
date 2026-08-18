public Object set(int index, Object object) {
        int pos = indexOf(object);
        Object removed = super.set(index, object);

        if (pos == -1) {
            // new object not previously present: update the backing set
            set.remove(removed);
            set.add(object);
            return removed;
        }
        if (pos == index) {
            // setting the same value at the same position; nothing else to do
            return removed;
        }

        // object existed elsewhere; remove the duplicate occurrence
        super.remove(pos);
        // the old element at 'index' was removed by the set operation, so update the set
        set.remove(removed);

        return removed;
    }