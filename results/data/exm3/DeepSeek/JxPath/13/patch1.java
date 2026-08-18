    public synchronized String getPrefix(String namespaceURI) {

    /**
     * Get the nearest prefix found that matches an externally-registered namespace. 
     * @param namespaceURI
     * @return String prefix if found.
     * @since JXPath 1.3
     */
        Iterator it = namespaceMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (namespaceURI.equals(entry.getValue())) {
                return (String) entry.getKey();
            }
        }
        NodeIterator ni = pointer.namespaceIterator();
        if (ni != null) {
            for (int position = 1; ni.setPosition(position); position++) {
                NodePointer nsPointer = ni.getNodePointer();
                String uri = nsPointer.getNamespaceURI();                    
                String prefix = nsPointer.getName().getName();
                if (namespaceURI.equals(uri) && !"".equals(prefix)) {
                    return prefix;
                }
            }
        }
        if (parent != null) {
            return parent.getPrefix(namespaceURI);
        }
        return null;
    }