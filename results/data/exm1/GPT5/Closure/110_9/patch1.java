  public Node getChildBefore(Node child) {
    if (child == first) {
      return null;
    }
    Node n = first;

    while (n != null && n.next != child) {
      n = n.next;
    }
    return n;
  }