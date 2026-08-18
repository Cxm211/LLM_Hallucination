public Object set(int index, Object object) {
        Object removed = super.get(index);
        int pos = indexOf(object);

        if (pos == -1 || pos == index) {
            super.set(index, object);
            set.remove(removed);
            set.add(object);
            return removed;
        }

        super.set(index, object);
        super.remove(pos);
        set.remove(removed);

        return removed;
    }