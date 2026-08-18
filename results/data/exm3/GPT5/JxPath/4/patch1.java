public Object getValue() {
        // Determine string-value honoring xml:space inheritance and ignoring comments/PIs per XPath
        return stringValue(node);
    }