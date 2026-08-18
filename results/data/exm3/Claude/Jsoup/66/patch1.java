protected Element doClone(Node parent) {
    Element clone = (Element) super.doClone(parent);
    clone.attributes = attributes != null ? attributes.clone() : null;
    clone.baseUri = baseUri;
    clone.childNodes = new NodeList(childNodes.size());
    for (Node child : childNodes) {
        clone.childNodes.add(child.clone());
    }

    return clone;
}