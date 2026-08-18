private boolean testAttr(Attr attr) {
    String nodePrefix = DOMNodePointer.getPrefix(attr);
    String nodeLocalName = DOMNodePointer.getLocalName(attr);

    if (nodePrefix != null && nodePrefix.equals("xmlns")) {
        return false;
    }

    if (nodePrefix == null && nodeLocalName.equals("xmlns")) {
        return false;
    }

    String testLocalName = name.getName();
    // Fix: if testLocalName is "*" and testPrefix is null, match all attributes
    if (testLocalName.equals("*") && name.getPrefix() == null) {
        return true;
    }
    if (testLocalName.equals("*") || testLocalName.equals(nodeLocalName)) {
        String testPrefix = name.getPrefix();

        if (equalStrings(testPrefix, nodePrefix)) {
            return true;
        }
        String testNS = null;
        if (testPrefix != null) {
            testNS = parent.getNamespaceURI(testPrefix);
        }
        String nodeNS = null;
        if (nodePrefix != null) {
            nodeNS = parent.getNamespaceURI(nodePrefix);
        }
        return equalStrings(testNS, nodeNS);
    }
    return false;
}