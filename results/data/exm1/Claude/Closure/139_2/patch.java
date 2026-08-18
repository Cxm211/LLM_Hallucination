public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()) {
        case Token.WHILE:
          if (CONVERT_WHILE_TO_FOR) {
            Node expr = n.getFirstChild();
            n.setType(Token.FOR);
            Node empty1 = new Node(Token.EMPTY);
            Node empty2 = new Node(Token.EMPTY);
            n.addChildBefore(empty1, expr);
            n.addChildAfter(empty2, expr);
            reportCodeChange("WHILE node");
          }
          break;

      }
    }