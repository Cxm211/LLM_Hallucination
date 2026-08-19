// ===== FIXED org.apache.commons.collections.list.SetUniqueList :: addAll(int, Collection) [lines 189-205] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-5-fixed/src/java/org/apache/commons/collections/list/SetUniqueList.java =====
    public boolean addAll(int index, Collection coll) {
        // gets initial size
        final int sizeBefore = size();

        // adds all elements
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            int sizeBeforeAddNext = size();
            add(index, it.next());
            // if it was inserted, then increase the target index
            if (sizeBeforeAddNext != size()) {
              index++;
            }
        }

        // compares sizes to detect if collection changed
        return sizeBefore != size();
    }
