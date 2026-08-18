Node processAssignment(Assignment assignmentNode) {
      Node assign = processInfixExpression(assignmentNode);
      Node left = assign.getFirstChild();
      Node right = left.getNext();
      if (right != null && right.getParent() != assign) {
        assign.replaceChild(right, right);
      }
      return assign;
    }