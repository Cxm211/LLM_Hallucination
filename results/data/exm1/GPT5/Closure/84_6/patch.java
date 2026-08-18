Node processAssignment(Assignment assignmentNode) {
      int type = transformTokenType(assignmentNode.getType());
      Node lhs = transform(assignmentNode.getLeft());
      Node rhs = transform(assignmentNode.getRight());
      Node assign = newNode(type, lhs, rhs);
      return assign;
    }