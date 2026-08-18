public boolean nextNode() {
        super.setPosition(getCurrentPosition() + 1);
        if (!setStarted) {
            setStarted = true;
            if (nodeTest instanceof NodeNameTest) {
                QName name = ((NodeNameTest) nodeTest).getNodeName();
                iterator = parentContext.getCurrentNodePointer().attributeIterator(name);
            } else if (nodeTest == null) {
                iterator = parentContext.getCurrentNodePointer().attributeIterator(null);
            } else if (nodeTest instanceof NodeTypeTest) {
                NodeTypeTest nt = (NodeTypeTest) nodeTest;
                if (nt.getNodeType() == Compiler.NODE_TYPE_NODE) {
                    iterator = parentContext.getCurrentNodePointer().attributeIterator(null);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (iterator == null) {
            return false;
        }
        if (!iterator.setPosition(iterator.getPosition() + 1)) {
            return false;
        }
        currentNodePointer = iterator.getNodePointer();
        return true;
    }