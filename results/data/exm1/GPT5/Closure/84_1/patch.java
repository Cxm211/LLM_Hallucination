Node processAssignment(Assignment assignmentNode) {
      int type = transformTokenType(assignmentNode.getType());
      Node left = transform(assignmentNode.getLeft());
      Node right = transform(assignmentNode.getRight());
      return newNode(type, left, right);
    }