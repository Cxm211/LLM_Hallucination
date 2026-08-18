public boolean nextNode() {
    if (!setStarted) {
        setStarted = true;
        if (!(nodeTest instanceof NodeNameTest)) {
            super.setPosition(getCurrentPosition() + 1);
            return false;
        }
        QName name = ((NodeNameTest) nodeTest).getNodeName();
        iterator =
            parentContext.getCurrentNodePointer().attributeIterator(name);
    }
    if (iterator == null) {
        super.setPosition(getCurrentPosition() + 1);
        return false;
    }
    if (!iterator.setPosition(iterator.getPosition() + 1)) {
        super.setPosition(getCurrentPosition() + 1);
        return false;
    }
    currentNodePointer = iterator.getNodePointer();
    super.setPosition(getCurrentPosition() + 1);
    return true;
}