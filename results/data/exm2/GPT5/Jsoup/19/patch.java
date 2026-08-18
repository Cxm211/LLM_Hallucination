private boolean testValidProtocol(Element el, Attribute attr, Set<Protocol> protocols) {
        String value = attr.getValue();
        String lcValue = value.toLowerCase();
        for (Protocol protocol : protocols) {
            String prot = protocol.toString() + ":";
            if (lcValue.startsWith(prot)) {
                return true;
            }
        }
        String abs = el.absUrl(attr.getKey());
        if (abs.length() == 0)
            return false;
        if (!preserveRelativeLinks)
            attr.setValue(abs);
        String lcAbs = abs.toLowerCase();
        for (Protocol protocol : protocols) {
            String prot = protocol.toString() + ":";
            if (lcAbs.startsWith(prot)) {
                return true;
            }
        }
        return false;
    }