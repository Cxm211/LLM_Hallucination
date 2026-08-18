public final Object computeValue(EvalContext context) {
    Object left = args[0].computeValue(context);
    Object right = args[1].computeValue(context);
    
    // Handle empty NodeSet cases
    if (left instanceof org.apache.commons.jxpath.ri.model.NodePointer[]) {
        org.apache.commons.jxpath.ri.model.NodePointer[] pointers = (org.apache.commons.jxpath.ri.model.NodePointer[]) left;
        if (pointers.length == 0) {
            return Boolean.FALSE;
        }
    }
    if (right instanceof org.apache.commons.jxpath.ri.model.NodePointer[]) {
        org.apache.commons.jxpath.ri.model.NodePointer[] pointers = (org.apache.commons.jxpath.ri.model.NodePointer[]) right;
        if (pointers.length == 0) {
            return Boolean.FALSE;
        }
    }
    
    // Handle empty collections/iterators
    if (left instanceof java.util.Collection && ((java.util.Collection) left).isEmpty()) {
        return Boolean.FALSE;
    }
    if (right instanceof java.util.Collection && ((java.util.Collection) right).isEmpty()) {
        return Boolean.FALSE;
    }
    
    if (left instanceof java.util.Iterator) {
        java.util.Iterator it = (java.util.Iterator) left;
        if (!it.hasNext()) {
            return Boolean.FALSE;
        }
    }
    if (right instanceof java.util.Iterator) {
        java.util.Iterator it = (java.util.Iterator) right;
        if (!it.hasNext()) {
            return Boolean.FALSE;
        }
    }
    
    return compute(left, right) ? Boolean.TRUE : Boolean.FALSE;
}