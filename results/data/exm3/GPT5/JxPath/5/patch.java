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

        if (depth1 == 1) {
            // Pointers belong to different trees; define a stable arbitrary ordering
            if (p1 == null) {
                return -1;
            }
            if (p2 == null) {
                return 1;
            }
            int h1 = System.identityHashCode(p1);
            int h2 = System.identityHashCode(p2);
            return h1 < h2 ? -1 : (h1 == h2 ? 0 : 1);
        }
        int r = compareNodePointers(p1.parent, depth1 - 1, p2.parent, depth2 - 1);
        if (r != 0) {
            return r;
        }

        return p1.parent.compareChildNodePointers(p1, p2);
    }