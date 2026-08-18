public NodePointer createPath(JXPathContext context, Object value) {
    NodePointer newParent = parent.createPath(context);
    if (isAttribute()) {
        NodePointer pointer = newParent.createAttribute(context, getName());
        pointer.setValue(value);
        return pointer;
    }
    else {
        if (newParent instanceof PropertyOwnerPointer) {
            return ((PropertyOwnerPointer) newParent).createChild(context, getName(), index, value);
        }
        return newParent.createChild(context, getName(), index, value);
    }
}