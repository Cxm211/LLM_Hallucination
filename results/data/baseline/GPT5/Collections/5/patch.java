public boolean addAll(int index, Collection coll) {
        if (coll == null) {
            throw new NullPointerException("Collection is null");
        }
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        }
        final int sizeBefore = size();
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            add(index++, it.next());
        }
        return sizeBefore != size();
    }