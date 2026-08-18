public List<Node> siblingNodes() {
        if (parent() == null)
            return new ArrayList<Node>();
        List<Node> nodes = parent().childNodes();
        List<Node> siblings = new ArrayList<Node>();
        for (Node n : nodes) {
            if (n != this)
                siblings.add(n);
        }
        return siblings;
    }