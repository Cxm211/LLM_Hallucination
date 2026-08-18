     include this node (a node is not a sibling of itself).
     @return node siblings. If the node has no parent, returns an empty list.
     */
    public List<Node> siblingNodes() {

        return parent().childNodes();
    }