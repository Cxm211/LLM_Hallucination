public boolean addAll(int index, Collection coll) {
        final int sizeBefore = size();
        int currentIndex = index;
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            int sizeBeforeAdd = size();
            add(currentIndex, it.next());
            if (size() > sizeBeforeAdd) {
                currentIndex++;
            }
        }
        return sizeBefore != size();
    }