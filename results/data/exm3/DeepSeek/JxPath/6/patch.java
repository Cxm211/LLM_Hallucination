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

        // Convert arrays to iterators
        if (l != null && l.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(l);
            java.util.List list = new java.util.ArrayList(length);
            for (int i = 0; i < length; i++) {
                list.add(java.lang.reflect.Array.get(l, i));
            }
            l = list.iterator();
        }
        if (r != null && r.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(r);
            java.util.List list = new java.util.ArrayList(length);
            for (int i = 0; i < length; i++) {
                list.add(java.lang.reflect.Array.get(r, i));
            }
            r = list.iterator();
        }

        if (l instanceof Collection) {
            l = ((Collection) l).iterator();
        }

        if (r instanceof Collection) {
            r = ((Collection) r).iterator();
        }

        // If either is null, delegate to object equality to avoid NPE in contains/findMatch
        if (l == null || r == null) {
            return equal(l, r);
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