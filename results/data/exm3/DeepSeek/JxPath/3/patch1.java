    public NodePointer createPath(JXPathContext context, Object value) {
        NodePointer newParent = parent.createPath(context);
        if (isAttribute()) {
            try {
                NodePointer pointer = newParent.createAttribute(context, getName());
                pointer.setValue(value);
                return pointer;
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
                return newParent.createChild(context, getName(), index, value);
            } catch (JXPathAbstractFactoryException e) {
                throw new JXPathException(e);
            }
        }
    }