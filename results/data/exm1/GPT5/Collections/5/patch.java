public boolean addAll(int index, Collection coll) {
        final int sizeBefore = size();
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            final Object obj = it.next();
            if (!contains(obj)) {
                add(index, obj);
                index++;
            }
        }
        return sizeBefore != size();
    }