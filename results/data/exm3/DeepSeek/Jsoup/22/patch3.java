    public List<Node> siblingNodes() {
        Node parent = parent();
        if (parent == null) {
            return new ArrayList<>();
        }
        List<Node> siblings = parent.childNodes();
        List<Node> result = new ArrayList<>(siblings.size() - 1);
        for (Node sibling : siblings) {
            if (sibling != this) {
                result.add(sibling);
            }
        }
        return result;
    }