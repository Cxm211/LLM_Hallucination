    private boolean testValidProtocol(Element el, Attribute attr, Set<Protocol> protocols) {
        // try to resolve relative urls to abs, and optionally update the attribute so output html has abs.
        // rels without a baseuri get removed
        String absUrl = el.absUrl(attr.getKey());
        String value;
        if (absUrl.length() == 0) {
            // couldn't make absolute, use the original value
            value = attr.getValue();
        } else {
            value = absUrl;
        }
        if (!preserveRelativeLinks && absUrl.length() > 0) {
            attr.setValue(absUrl);
        }

        for (Protocol protocol : protocols) {
            String prot = protocol.toString() + ":";
            if (value.toLowerCase().startsWith(prot)) {
                return true;
            }
        }
        return false;
    }