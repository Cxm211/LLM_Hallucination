public boolean nextNode() {
        if (!setStarted) {
            setStarted = true;
            QName name = null;
            if (nodeTest instanceof NodeNameTest) {
                name = ((NodeNameTest) nodeTest).getNodeName();
            } else if (nodeTest instanceof NodeTypeTest) {
                // wildcard attribute test like @*
                name = null;
            } else {
                return false;
            }
            iterator = parentContext.getCurrentNodePointer().attributeIterator(name);
        }
        if (iterator == null) {
            return false;
        }
        if (!iterator.setPosition(iterator.getPosition() + 1)) {
            return false;
        }
        currentNodePointer = iterator.getNodePointer();
        super.setPosition(getCurrentPosition() + 1);
        return true;
    }