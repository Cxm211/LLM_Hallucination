public boolean addAll(int index, Collection coll) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        }

        final int sizeBefore = size();
        int insertIndex = index;

        for (final Iterator it = coll.iterator(); it.hasNext();) {
            Object obj = it.next();
            if (!contains(obj)) {
                add(insertIndex, obj);
                insertIndex++;
            }
        }

        return sizeBefore != size();
    }