public synchronized String getPrefix(String namespaceURI) {
        if (reverseMap == null) {
            reverseMap = new HashMap();
            if (pointer != null) {
                NodeIterator ni = pointer.namespaceIterator();
                if (ni != null) {
                    for (int position = 1; ni.setPosition(position); position++) {
                        NodePointer nsPointer = ni.getNodePointer();
                        String uri = nsPointer.getNamespaceURI();
                        String prefix = nsPointer.getName().getName();
                        if (!"".equals(prefix)) {
                            reverseMap.put(uri, prefix);
                        }
                    }
                }
            }
            Iterator it = namespaceMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                reverseMap.put(entry.getValue(), entry.getKey());
            }
        }
        String prefix = (String) reverseMap.get(namespaceURI);
        if (prefix == null && parent != null) {
            return parent.getPrefix(namespaceURI);
        }
        return prefix;
    }