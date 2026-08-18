public List<Node> siblingNodes() {
    if (parentNode == null) return Collections.emptyList();
    List<Node> siblings = parentNode.childNodes;
    List<Node> result = new ArrayList<Node>(siblings);
    result.remove(this);
    return result;
}