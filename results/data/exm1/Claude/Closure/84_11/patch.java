Node processAssignment(Assignment assignmentNode) {
      Node assign = processInfixExpression(assignmentNode);
      Node left = assign.getFirstChild();
      Node right = assign.getLastChild();
      
      if (left != null && right != null && 
          left.isName() && right.isName() && 
          left.getString().equals(right.getString())) {
        return right;
      }
      return assign;
    }