public Node getChildBefore(Node child) {
    if (child == first) {
      return null;
    }
    if (first == null) {
      if (child == null) {
        return null;
      }
      throw new RuntimeException("node is not a child");
    }
    Node n = first;

    while (n.next != child) {
      n = n.next;
      if (n == null) {
        throw new RuntimeException("node is not a child");
      }
    }
    return n;
  }