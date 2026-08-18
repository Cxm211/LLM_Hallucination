public boolean nextNode() {
        super.setPosition(getCurrentPosition() + 1);
        if (!setStarted) {
            setStarted = true;
            if (nodeTest instanceof NodeNameTest) {
                QName name = ((NodeNameTest) nodeTest).getNodeName();
                iterator =
                    parentContext.getCurrentNodePointer().attributeIterator(name);
            } else if (nodeTest == null || nodeTest instanceof NodeTypeTest) {
                iterator =
                    parentContext.getCurrentNodePointer().attributeIterator(null);
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