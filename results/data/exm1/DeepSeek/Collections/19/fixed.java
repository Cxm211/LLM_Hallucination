// ===== FIXED org.apache.commons.collections.list.SetUniqueList :: set(int, E) [lines 227-242] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-19-fixed/src/main/java/org/apache/commons/collections/list/SetUniqueList.java =====
    public E set(final int index, final E object) {
        final int pos = indexOf(object);
        final E removed = super.set(index, object);

        if (pos != -1 && pos != index) {
            // the object is already in the uniq list
            // (and it hasn't been swapped with itself)
            super.remove(pos); // remove the duplicate by index
            set.remove(removed); // remove the item deleted by the set
        } else if (pos == -1) {
            set.add(object); // add the new item to the unique set
            set.remove(removed); // remove the item deleted by the set
        }
        
        return removed; // return the item deleted by the set
    }
