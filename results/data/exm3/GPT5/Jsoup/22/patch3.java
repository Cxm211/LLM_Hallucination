public List<Node> siblingNodes() {

        Node parent = parent();
        if (parent == null)
            return new ArrayList<Node>(0);
        List<Node> nodes = parent.childNodes();
        List<Node> siblings = new ArrayList<Node>();
        for (Node node : nodes) {
            if (node != this)
                siblings.add(node);
        }
        return siblings;
    }