    boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        if (block.isFunction) {
          break;
        } else if (block.isLoop) {
          return false;
        }
      }

      return true;
    }

// trigger testcase
public void testExternalIssue1053() {
    testSame(
        "var u; function f() { u = Random(); var x = u; f(); alert(x===u)}");
  }
