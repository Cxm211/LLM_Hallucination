    public boolean addAll(int index, Collection coll) {
        // gets initial size
        final int sizeBefore = size();

        // adds all elements
        int insertIndex = index;
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            if (add(insertIndex, it.next())) {
                insertIndex++;
            }
            // if it was inserted, then increase the target index
        }

        // compares sizes to detect if collection changed
        return sizeBefore != size();
    }