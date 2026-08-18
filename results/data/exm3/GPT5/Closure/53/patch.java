private void replaceAssignmentExpression(Var v, Reference ref,
                                             Map<String, String> varmap) {
      // Compute all of the assignments necessary
      List<Node> nodes = Lists.newArrayList();
      Node val = ref.getAssignedValue();
      blacklistVarReferencesInTree(val, v.scope);
      Preconditions.checkState(val.getType() == Token.OBJECTLIT);
      Set<String> all = Sets.newLinkedHashSet(varmap.keySet());
      for (Node key = val.getFirstChild(); key != null;
           key = key.getNext()) {
        String var = key.getString();
        Node value = key.removeFirstChild();
        // TODO(user): Copy type information.
        nodes.add(
          new Node(Token.ASSIGN,
                   Node.newString(Token.NAME, varmap.get(var)), value));
        all.remove(var);
      }

      // TODO(user): Better source information.
      for (String var : all) {
        nodes.add(
          new Node(Token.ASSIGN,
                   Node.newString(Token.NAME, varmap.get(var)),
                   NodeUtil.newUndefinedNode(null)));
      }

      Node replace = ref.getParent();
      boolean isVar = replace.getType() == Token.VAR;

      // All assignments evaluate to true; only add TRUE to preserve value for
      // expression statements (non-VAR). For VAR declarations, there's no
      // expression value to preserve.
      if (!isVar) {
        nodes.add(new Node(Token.TRUE));
      }

      Node replacement = null;
      if (!nodes.isEmpty()) {
        if (nodes.size() == 1) {
          replacement = nodes.get(0);
        } else {
          // Join these using COMMA. A COMMA node must have 2 children, so we
          // create a tree. In the tree the first child be the COMMA to match
          // the parser, otherwise tree equality tests fail.
          nodes = Lists.reverse(nodes);
          replacement = new Node(Token.COMMA);
          Node cur = replacement;
          int i = 0;
          for (; i < nodes.size() - 2; i++) {
            cur.addChildToFront(nodes.get(i));
            Node t = new Node(Token.COMMA);
            cur.addChildToFront(t);
            cur = t;
          }
          cur.addChildToFront(nodes.get(i));
          cur.addChildToFront(nodes.get(i + 1));
        }
      }

      if (replacement != null) {
        replacement.copyInformationFromForTree(replace);
      }

      if (isVar) {
        if (replacement == null) {
          // No assignments and VAR declaration becomes empty; remove it.
          replace.getParent().removeChild(replace);
        } else {
          replace.getParent().replaceChild(
              replace, NodeUtil.newExpr(replacement));
        }
      } else {
        replace.getParent().replaceChild(replace, replacement);
      }
    }