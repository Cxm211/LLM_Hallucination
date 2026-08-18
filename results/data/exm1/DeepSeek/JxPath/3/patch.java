public NodePointer createPath(JXPathContext context) {
    NodePointer newParent = parent.createPath(context);
    if (isAttribute()) {
        return newParent.createAttribute(context, getName());
    }
    else {
        if (newParent instanceof PropertyOwnerPointer) {
            return ((PropertyOwnerPointer) newParent).createChild(context, getName(), getIndex());
        }
        return newParent.createChild(context, getName(), getIndex());
    }
}