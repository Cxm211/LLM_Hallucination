Node processAssignment(Assignment assignmentNode) {
      // Build an assignment node directly to ensure correct AST for downstream passes
      Node left = transform(assignmentNode.getLeft());
      Node right = transform(assignmentNode.getRight());
      return newNode(Token.ASSIGN, left, right);
    }