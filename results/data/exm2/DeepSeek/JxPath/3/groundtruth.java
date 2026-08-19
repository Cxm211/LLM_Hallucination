// ===== FIXED org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer :: createPath(JXPathContext) [lines 105-133] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-3-fixed/src/java/org/apache/commons/jxpath/ri/model/beans/NullPropertyPointer.java =====
    public NodePointer createPath(JXPathContext context) {
        NodePointer newParent = parent.createPath(context);
        if (isAttribute()) {
            return newParent.createAttribute(context, getName());
        }
        else {
            if (parent instanceof NullPointer && parent.equals(newParent)) {
                throw createBadFactoryException(context.getFactory());
            }
            // Consider these two use cases:
            // 1. The parent pointer of NullPropertyPointer is 
            //    a PropertyOwnerPointer other than NullPointer. When we call 
            //    createPath on it, it most likely returns itself. We then
            //    take a PropertyPointer from it and get the PropertyPointer
            //    to expand the collection for the corresponding property.
            //
            // 2. The parent pointer of NullPropertyPointer is a NullPointer.
            //    When we call createPath, it may return a PropertyOwnerPointer
            //    or it may return anything else, like a DOMNodePointer.
            //    In the former case we need to do exactly what we did in use 
            //    case 1.  In the latter case, we simply request that the 
            //    non-property pointer expand the collection by itself.
            if (newParent instanceof PropertyOwnerPointer) {
                PropertyOwnerPointer pop = (PropertyOwnerPointer) newParent;
                newParent = pop.getPropertyPointer();
            }
            return newParent.createChild(context, getName(), getIndex());
        }
    }

// ===== FIXED org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer :: createPath(JXPathContext, Object) [lines 135-152] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-3-fixed/src/java/org/apache/commons/jxpath/ri/model/beans/NullPropertyPointer.java =====
    public NodePointer createPath(JXPathContext context, Object value) {
        NodePointer newParent = parent.createPath(context);
        if (isAttribute()) {
            NodePointer pointer = newParent.createAttribute(context, getName());
            pointer.setValue(value);
            return pointer;
        }
        else {
            if (parent instanceof NullPointer && parent.equals(newParent)) {
                throw createBadFactoryException(context.getFactory());
            }
            if (newParent instanceof PropertyOwnerPointer) {
                PropertyOwnerPointer pop = (PropertyOwnerPointer) newParent;
                newParent = pop.getPropertyPointer();
            }
            return newParent.createChild(context, getName(), index, value);
        }
    }

// ===== FIXED org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer :: escape(String) [lines 213-231] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JxPath/JxPath-3-fixed/src/java/org/apache/commons/jxpath/ri/model/beans/NullPropertyPointer.java =====
    private String escape(String string) {
        int index = string.indexOf('\'');
        while (index != -1) {
            string =
                string.substring(0, index)
                    + "&apos;"
                    + string.substring(index + 1);
            index = string.indexOf('\'');
        }
        index = string.indexOf('\"');
        while (index != -1) {
            string =
                string.substring(0, index)
                    + "&quot;"
                    + string.substring(index + 1);
            index = string.indexOf('\"');
        }
        return string;
    }
