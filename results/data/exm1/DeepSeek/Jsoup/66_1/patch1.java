protected Element doClone(Node parent) {
        Element clone = (Element) super.doClone(parent);
        clone.attributes = attributes != null ? attributes.clone() : null;
        clone.baseUri = baseUri;
        clone.childNodes = new NodeList(childNodes.size());
        clone.childNodes.addAll(childNodes);
        for (Node child : clone.childNodes) {
            child.setParentNode(clone);
        }
        return clone;
    }