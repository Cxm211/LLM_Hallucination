Node processAssignment(Assignment assignmentNode) {
      Node left = transform(assignmentNode.getLeft());
      Node right = transform(assignmentNode.getRight());
      int type = transformTokenType(assignmentNode.getType());
      Node assign = newNode(type, left, right);
      return assign;
    }