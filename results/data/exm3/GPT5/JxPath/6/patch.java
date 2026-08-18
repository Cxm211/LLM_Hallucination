protected boolean equal(
        EvalContext context,
        Expression left,
        Expression right) 
    {
        Object l = left.compute(context);
        Object r = right.compute(context);

        if (l instanceof InitialContext || l instanceof SelfContext) {
            l = ((EvalContext) l).getSingleNodePointer();
        }

        if (r instanceof InitialContext || r instanceof SelfContext) {
            r = ((EvalContext) r).getSingleNodePointer();
        }

        // Treat arrays as iterables
        if (l != null && l.getClass().isArray()) {
            final Object arr = l;
            final int len = java.lang.reflect.Array.getLength(arr);
            l = new java.util.Iterator() {
                int i = 0;
                public boolean hasNext() { return i < len; }
                public Object next() {
                    if (i >= len) { throw new java.util.NoSuchElementException(); }
                    return java.lang.reflect.Array.get(arr, i++);
                }
                public void remove() { throw new UnsupportedOperationException(); }
            };
        }

        if (r != null && r.getClass().isArray()) {
            final Object arr = r;
            final int len = java.lang.reflect.Array.getLength(arr);
            r = new java.util.Iterator() {
                int i = 0;
                public boolean hasNext() { return i < len; }
                public Object next() {
                    if (i >= len) { throw new java.util.NoSuchElementException(); }
                    return java.lang.reflect.Array.get(arr, i++);
                }
                public void remove() { throw new UnsupportedOperationException(); }
            };
        }

        if (l instanceof Collection) {
            l = ((Collection) l).iterator();
        }

        if (r instanceof Collection) {
            r = ((Collection) r).iterator();
        }

        if ((l instanceof Iterator) && !(r instanceof Iterator)) {
            return contains((Iterator) l, r);
        }
        if (!(l instanceof Iterator) && (r instanceof Iterator)) {
            return contains((Iterator) r, l);
        }
        if (l instanceof Iterator && r instanceof Iterator) {
            return findMatch((Iterator) l, (Iterator) r);
        }
        return equal(l, r);
    }