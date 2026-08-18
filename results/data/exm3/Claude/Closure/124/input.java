// buggy function
  private boolean isSafeReplacement(Node node, Node replacement) {
    // No checks are needed for simple names.
    if (node.isName()) {
      return true;
    }
    Preconditions.checkArgument(node.isGetProp());

      node = node.getFirstChild();
    if (node.isName()
        && isNameAssignedTo(node.getString(), replacement)) {
      return false;
    }

    return true;
  }

// trigger testcase
// com/google/javascript/jscomp/ExploitAssignsTest.java::testIssue1017
public void testIssue1017() {
    testSame("x = x.parentNode.parentNode; x = x.parentNode.parentNode;");
  }
