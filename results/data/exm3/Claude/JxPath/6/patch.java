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

    if ((l instanceof Iterator) && !(r instanceof Iterator)) {
        return contains((Iterator) l, r);
    }
    if (!(l instanceof Iterator) && (r instanceof Iterator)) {
        return contains((Iterator) r, l);
    }
    if (l instanceof Iterator && r instanceof Iterator) {
        Iterator lIter = (Iterator) l;
        Iterator rIter = (Iterator) r;
        while (lIter.hasNext()) {
            Object lValue = lIter.next();
            while (rIter.hasNext()) {
                Object rValue = rIter.next();
                if (equal(lValue, rValue)) {
                    return true;
                }
            }
            if (r instanceof Collection) {
                rIter = ((Collection) r).iterator();
            }
        }
        return false;
    }
    return equal(l, r);
}