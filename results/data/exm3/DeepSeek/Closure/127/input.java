// buggy function
    private void tryRemoveUnconditionalBranching(Node n) {
      /*
       * For each unconditional branching control flow node, check to see
       * if the ControlFlowAnalysis.computeFollowNode of that node is same as
       * the branching target. If it is, the branch node is safe to be removed.
       *
       * This is not as clever as MinimizeExitPoints because it doesn't do any
       * if-else conversion but it handles more complicated switch statements
       * much more nicely.
       */

      // If n is null the target is the end of the function, nothing to do.
      if (n == null) {
         return;
      }

      DiGraphNode<Node, Branch> gNode = cfg.getDirectedGraphNode(n);

      if (gNode == null) {
        return;
      }

      switch (n.getType()) {
        case Token.RETURN:
          if (n.hasChildren()) {
            break;
          }
        case Token.BREAK:
        case Token.CONTINUE:
          // We are looking for a control flow changing statement that always
          // branches to the same node. If after removing it control still
          // branches to the same node, it is safe to remove.
          List<DiGraphEdge<Node, Branch>> outEdges = gNode.getOutEdges();
          if (outEdges.size() == 1 &&
              // If there is a next node, this jump is not useless.
              (n.getNext() == null || n.getNext().isFunction())) {

            Preconditions.checkState(
                outEdges.get(0).getValue() == Branch.UNCOND);
            Node fallThrough = computeFollowing(n);
            Node nextCfgNode = outEdges.get(0).getDestination().getValue();
            if (nextCfgNode == fallThrough) {
              removeNode(n);
            }
          }
      }
    }

// trigger testcase
// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java::testDontRemoveBreakInTryFinally
public void testDontRemoveBreakInTryFinally() throws Exception {
    testSame("function f() {b:try{throw 9} finally {break b} return 1;}");
  }

// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java::testDontRemoveBreakInTryFinallySwitch
public void testDontRemoveBreakInTryFinallySwitch() throws Exception {
    testSame("function f() {b:try{throw 9} finally {switch(x) {case 1: break b} } return 1;}");
  }

// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java::testIssue4177428_continue
public void testIssue4177428_continue() {
    testSame(
        "f = function() {\n" +
        "  var action;\n" +
        "  a: do {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      continue a\n" +  // Keep this...
        "    }\n" +
        "  } while(false)\n" +
        "  alert(action)\n" + // and this.
        "};");
  }

// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java::testIssue4177428_return
public void testIssue4177428_return() {
    test(
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      return\n" +  // Keep this...
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" + // and remove this.
        "};",
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      return\n" +
        "    }\n" +
        "  }\n" +
        "};"
        );
  }

// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java::testIssue4177428a
public void testIssue4177428a() {
    testSame(
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      break a\n" +  // Keep this...
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" + // and this.
        "};");
  }

// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java::testIssue4177428c
public void testIssue4177428c() {
    testSame(
        "f = function() {\n" +
        "  var action;\n" +
        "  a: {\n" +
        "    var proto = null;\n" +
        "    try {\n" +
        "    } finally {\n" +
        "    try {\n" +
        "      proto = new Proto\n" +
        "    } finally {\n" +
        "      action = proto;\n" +
        "      break a\n" +  // Keep this...
        "    }\n" +
        "    }\n" +
        "  }\n" +
        "  alert(action)\n" + // and this.
        "};");
  }
