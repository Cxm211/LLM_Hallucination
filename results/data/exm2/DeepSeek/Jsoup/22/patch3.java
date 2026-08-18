    public List<Node> siblingNodes() {
        Node parent = parent();
        if (parent == null) return new ArrayList<Node>();
        List<Node> siblings = parent.childNodes();
        Integer index = siblingIndex();
        Validate.notNull(index);
        List<Node> result = new ArrayList<Node>();
        for (int i = 0; i < siblings.size(); i++) {
            if (i != index) {
                result.add(siblings.get(i));
            }
        }
        return result;
    }