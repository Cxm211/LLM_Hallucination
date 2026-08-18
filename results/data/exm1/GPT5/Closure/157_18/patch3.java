    public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()) {
        case Token.GETPROP:
        case Token.GETELEM:
          Node dest = n.getFirstChild().getNext();
          if (dest.getType() == Token.STRING) {
            String s = dest.getString();
            if (s.equals("prototype")) {
              processPrototypeParent(parent, t.getInput());
            } else {
              markPropertyAccessCandidate(dest, t.getInput());
            }
          }
          break;
        case Token.OBJECTLIT:
          if (!prototypeObjLits.contains(n)) {
            // Only consider actual property name nodes (STRING/GET/SET).
            for (Node child = n.getFirstChild();
                 child != null;
                 child = child.getNext()) {

              int ct = child.getType();
              if (ct == Token.STRING || ct == Token.GET || ct == Token.SET) {
                markObjLitPropertyCandidate(child, t.getInput());
              }
            }
          }
          break;
      }
    }
