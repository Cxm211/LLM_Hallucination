    public Node getFunctionNode() {
      Node parent = nameNode.getParent();

      if (parent.isFunction()) {
        return parent;
      } else {
        // we are the name of a var node, so the function is name's second child
        return nameNode.getChildAtIndex(1);
      }
    }
