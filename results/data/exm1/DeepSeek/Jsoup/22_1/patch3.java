public List<Node> siblingNodes() {
    List<Node> siblings = parent().childNodes();
    List<Node> result = new ArrayList<Node>();
    for (Node sibling : siblings) {
        if (sibling != this)
            result.add(sibling);
    }
    return result;
}