// ===== FIXED org.apache.commons.collections.set.ListOrderedSet :: retainAll(Collection) [lines 228-249] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-18-fixed/src/main/java/org/apache/commons/collections/set/ListOrderedSet.java =====
    public boolean retainAll(Collection<?> coll) {
        Set<Object> collectionRetainAll = new HashSet<Object>();
        for (Iterator<?> it = coll.iterator(); it.hasNext();) {
            Object next = it.next();
            if (collection.contains(next)) {
                collectionRetainAll.add(next);
            }
        }
        if (collectionRetainAll.size() == collection.size()) {
            return false;
        }
        if (collectionRetainAll.size() == 0) {
            clear();
        } else {
            for (Iterator<E> it = iterator(); it.hasNext();) {
                if (!collectionRetainAll.contains(it.next())) {
                    it.remove();
                }
            }
        }
        return true;
    }
