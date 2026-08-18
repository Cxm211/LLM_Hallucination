    public boolean nextNode() {
        super.setPosition(getCurrentPosition() + 1);
        if (!setStarted) {
            setStarted = true;
            QName name = null;
            if (nodeTest instanceof NodeNameTest) {
                name = ((NodeNameTest) nodeTest).getNodeName();
            }
            iterator = parentContext.getCurrentNodePointer().attributeIterator(name);
        }
        if (iterator == null) {
            return false;
        }
        while (true) {
            if (!iterator.setPosition(iterator.getPosition() + 1)) {
                return false;
            }
            currentNodePointer = iterator.getNodePointer();
            if (nodeTest == null || nodeTest.testNode(currentNodePointer)) {
                return true;
            }
            // continue to next attribute
        }
    }