protected Element doClone(Node parent) {
    Element clone = (Element) super.doClone(parent);
    clone.attributes = attributes != null ? attributes.clone() : null;
    clone.baseUri = baseUri;
    clone.childNodes = clone.new NodeList(childNodes.size());
    for (Node child : childNodes) {
        Node childClone = child.clone();
        clone.childNodes.add(childClone);
        childClone.parentNode = clone;
    }
    return clone;
}