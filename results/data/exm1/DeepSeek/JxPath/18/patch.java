public boolean nextNode() {
    if (!setStarted) {
        setStarted = true;
        if (!(nodeTest instanceof NodeNameTest)) {
            return false;
        }
        QName name = ((NodeNameTest) nodeTest).getNodeName();
        iterator = parentContext.getCurrentNodePointer().attributeIterator(name);
    }
    if (iterator == null) {
        return false;
    }
    if (!iterator.setPosition(iterator.getPosition() + 1)) {
        return false;
    }
    super.setPosition(getCurrentPosition() + 1);
    currentNodePointer = iterator.getNodePointer();
    return true;
}