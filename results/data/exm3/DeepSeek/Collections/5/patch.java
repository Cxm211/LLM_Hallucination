    public boolean addAll(int index, Collection coll) {
        // gets initial size
        final int sizeBefore = size();

        // adds all elements
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            Object element = it.next();
            int currentSizeBeforeAdd = size();
            add(index, element);
            // if it was inserted, then increase the target index
            if (size() > currentSizeBeforeAdd) {
                index++;
            }
        }

        // compares sizes to detect if collection changed
        return sizeBefore != size();
    }