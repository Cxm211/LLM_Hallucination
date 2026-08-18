    public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()) {
        case Token.WHILE:
          if (CONVERT_WHILE_TO_FOR) {
            Node expr = n.getFirstChild();
            Node body = expr.getNext();
            n.removeChild(expr);
            n.removeChild(body);
            n.setType(Token.FOR);
            n.addChildToFront(new Node(Token.EMPTY));
            n.addChildAfter(expr, n.getFirstChild());
            n.addChildAfter(new Node(Token.EMPTY), expr);
            parent.addChildAfter(body, n);
            reportCodeChange("WHILE node");
          }
          break;
      }
    }