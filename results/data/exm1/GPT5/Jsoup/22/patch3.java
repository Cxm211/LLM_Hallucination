public List<Node> siblingNodes() {
        if (parentNode == null)
            return java.util.Collections.emptyList();
        List<Node> siblings = new java.util.ArrayList<Node>(Math.max(0, parentNode.childNodes.size() - 1));
        for (Node node : parentNode.childNodes) {
            if (node != this)
                siblings.add(node);
        }
        return siblings;
    }