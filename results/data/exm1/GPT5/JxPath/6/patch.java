protected boolean equal(
        EvalContext context,
        Expression left,
        Expression right) 
    {
        Object l = left.compute(context);
        Object r = right.compute(context);

//        System.err.println("COMPARING: " +
//            (l == null ? "null" : l.getClass().getName()) + " " +
//            (r == null ? "null" : r.getClass().getName()));

        if (l instanceof InitialContext || l instanceof SelfContext) {
            l = ((EvalContext) l).getSingleNodePointer();
        }

        if (r instanceof InitialContext || r instanceof SelfContext) {
            r = ((EvalContext) r).getSingleNodePointer();
        }

        if (l instanceof Collection) {
            l = ((Collection) l).iterator();
        }

        if (r instanceof Collection) {
            r = ((Collection) r).iterator();
        }

        if (l != null && l.getClass().isArray()) {
            final Object arr = l;
            l = new Iterator() {
                private int i = 0;
                private final int len = java.lang.reflect.Array.getLength(arr);
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
            r = new Iterator() {
                private int i = 0;
                private final int len = java.lang.reflect.Array.getLength(arr);
                public boolean hasNext() { return i < len; }
                public Object next() {
                    if (i >= len) { throw new java.util.NoSuchElementException(); }
                    return java.lang.reflect.Array.get(arr, i++);
                }
                public void remove() { throw new UnsupportedOperationException(); }
            };
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