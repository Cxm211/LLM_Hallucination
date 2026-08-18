public NodePointer createPath(JXPathContext context) {
    try {
        NodePointer newParent = parent.createPath(context);
        if (isAttribute()) {
            return newParent.createAttribute(context, getName());
        }
        else {
            if (newParent instanceof PropertyOwnerPointer) {
                PropertyOwnerPointer pop = (PropertyOwnerPointer) newParent;
                newParent = pop.getPropertyPointer();
            }
            return newParent.createChild(context, getName(), getIndex());
        }
    } catch (JXPathAbstractFactoryException ex) {
        throw new JXPathException(ex);
    }
}