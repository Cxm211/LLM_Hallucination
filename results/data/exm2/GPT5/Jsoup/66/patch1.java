    protected Element doClone(Node parent) {
        Element clone = (Element) super.doClone(parent);
        clone.attributes = attributes != null ? attributes.clone() : null;
        clone.baseUri = baseUri;
        clone.childNodes = new NodeList(childNodes.size());
        for (int i = 0; i < childNodes.size(); i++) {
            Node childClone = childNodes.get(i).clone();
            clone.childNodes.add(childClone);
        }

        return clone;
    }