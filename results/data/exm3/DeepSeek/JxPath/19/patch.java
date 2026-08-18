    private int getRelativePositionByQName() {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return 1;
        }
        int count = 1;
        Node n = node.getPreviousSibling();
        String ns = node.getNamespaceURI();
        String local = node.getLocalName();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String siblingNs = n.getNamespaceURI();
                String siblingLocal = n.getLocalName();
                boolean nsMatch = (ns == null) ? (siblingNs == null) : ns.equals(siblingNs);
                boolean localMatch = (local != null) && local.equals(siblingLocal);
                if (nsMatch && localMatch) {
                    count++;
                }
            }
            n = n.getPreviousSibling();
        }
        return count;
    }