    private int compareNodePointers(
        NodePointer p1,
        int depth1,
        NodePointer p2,
        int depth2) 
    {
        if (depth1 < depth2) {
            int r = compareNodePointers(p1, depth1, p2.parent, depth2 - 1);
            return r == 0 ? -1 : r;
        }
        if (depth1 > depth2) {
            int r = compareNodePointers(p1.parent, depth1 - 1, p2, depth2);
            return r == 0 ? 1 : r;
        }
        if (p1 == null && p2 == null) {
            return 0;
        }

        if (p1 != null && p1.equals(p2)) {
            return 0;
        }

        int r = compareNodePointers(p1.parent, depth1 - 1, p2.parent, depth2 - 1);
        if (r != 0) {
            return r;
        }

        if (p1.parent == null) {
            // Both parents are null, compare p1 and p2 by identity
            // Since they are not equal, we need a deterministic order
            return Integer.compare(System.identityHashCode(p1), System.identityHashCode(p2));
        }

        return p1.parent.compareChildNodePointers(p1, p2);
    }