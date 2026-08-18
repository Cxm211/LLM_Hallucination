    public NodePointer createPath(JXPathContext context) {
        NodePointer newParent = parent.createPath(context);
        if (isAttribute()) {
            try {
                return newParent.createAttribute(context, getName());
            } catch (JXPathAbstractFactoryException e) {
                throw new JXPathException(e);
            }
        }
        else {
            if (newParent instanceof PropertyOwnerPointer) {
                PropertyOwnerPointer pop = (PropertyOwnerPointer) newParent;
                newParent = pop.getPropertyPointer();
            }
            try {
                return newParent.createChild(context, getName(), getIndex());
            } catch (JXPathAbstractFactoryException e) {
                throw new JXPathException(e);
            }
        }
    }