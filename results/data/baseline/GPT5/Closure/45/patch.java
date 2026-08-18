private void interpretAssigns() {
  boolean changes = false;
  do {
    changes = false;

    for (int current = 0; current < maybeUnreferenced.size(); current++) {
      Var var = maybeUnreferenced.get(current);
      if (referenced.contains(var)) {
        maybeUnreferenced.remove(current);
        current--;
      } else {
        boolean assignedToUnknownValue = false;
        boolean hasPropertyAssign = false;

        if (var.getParentNode().isVar() &&
            !NodeUtil.isForIn(var.getParentNode().getParent())) {
          Node value = var.getInitialValue();
          assignedToUnknownValue = value != null &&
              !NodeUtil.isLiteralValue(value, true);
        } else {
          assignedToUnknownValue = true;
        }

        java.util.List<Assign> assigns = assignsByVar.get(var);
        if (assigns != null) {
          for (Assign assign : assigns) {
            if (assign.isPropertyAssign) {
              hasPropertyAssign = true;
            } else if (!NodeUtil.isLiteralValue(
                assign.assignNode.getLastChild(), true)) {
              assignedToUnknownValue = true;
            }
          }
        }

        if (assignedToUnknownValue && hasPropertyAssign) {
          changes = markReferencedVar(var) || changes;
          maybeUnreferenced.remove(current);
          current--;
        }
      }
    }
  } while (changes);
}