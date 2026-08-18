private boolean testValidProtocol(Element el, Attribute attr, Set<Protocol> protocols) {
        // try to resolve relative urls to abs, and optionally update the attribute so output html has abs.
        // rels without a baseuri get removed
        String value = el.absUrl(attr.getKey());
        if (value.length() == 0)
            value = attr.getValue();
        
        for (Protocol protocol : protocols) {
            String prot = protocol.toString() + ":";
            if (value.toLowerCase().startsWith(prot)) {
                if (!preserveRelativeLinks)
                    attr.setValue(value);
                return true;
            }
        }
        return false;
    }