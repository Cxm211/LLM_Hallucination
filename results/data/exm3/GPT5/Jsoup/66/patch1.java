protected Element doClone(Node parent) {
        Element clone = (Element) super.doClone(parent);
        clone.attributes = attributes != null ? attributes.clone() : null;
        clone.baseUri = baseUri;
        int size = childNodes != null ? childNodes.size() : 0;
        clone.childNodes = new NodeList(size);
        if (size > 0) {
            for (int i = 0; i < childNodes.size(); i++) {
                Node child = childNodes.get(i);
                if (child != null) {
                    Node childClone = child.doClone(clone);
                    if (childClone != null) {
                        clone.childNodes.add(childClone);
                    }
                }
            }
        }
        return clone;
    }