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
        // depth1 == depth2
        if (p1 == null && p2 == null) {
            return 0;
        }
        if (p1 != null && p1.equals(p2)) {
            return 0;
        }
        // At root level (depth 0), pointers are different roots.
        if (depth1 == 0) {
            // Need to establish a deterministic order.
            // Use identity hash code as a tie-breaker.
            int h1 = System.identityHashCode(p1);
            int h2 = System.identityHashCode(p2);
            if (h1 < h2) {
                return -1;
            }
            if (h1 > h2) {
                return 1;
            }
            // In the rare case of identity hash collision, return 0.
            // This may treat different pointers as equal, but it's extremely unlikely.
            return 0;
        }
        // depth > 0, both have parents.
        int r = compareNodePointers(p1.parent, depth1 - 1, p2.parent, depth2 - 1);
        if (r != 0) {
            return r;
        }
        // Parents are equal.
        return p1.parent.compareChildNodePointers(p1, p2);
    }