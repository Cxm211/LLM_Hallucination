    private boolean isInlinableObject(List<Reference> refs) {
      Set<String> propNames = new HashSet<String>();
      boolean hasAssignment = false;

      // First pass: collect property names from object literals and check assignments.
      for (Reference ref : refs) {
        Node name = ref.getNode();
        Node parent = ref.getParent();
        Node gramps = ref.getGrandparent();

        if (parent.isGetProp()) {
          // We'll handle GETPROP references in the second pass.
          continue;
        }

        // Only rewrite VAR declarations or simple assignment statements
        if (!isVarOrAssignExprLhs(name)) {
           return false;
        }

        Node val = ref.getAssignedValue();
        if (val == null) {
          // A var with no assignment.
          continue;
        }

        // We're looking for object literal assignments only.
        if (!val.isObjectLit()) {
          return false;
        }

        // Make sure that the value is not self‑referential. IOW,
        // disallow things like x = {b: x.a}.
        //
        // TODO: Only exclude unorderable self‑referential
        // assignments. i.e. x = {a: x.b, b: x.a} is not orderable,
        // but x = {a: 1, b: x.a} is.
        //
        // Also, ES5 getters/setters aren't handled by this pass.
        for (Node child = val.getFirstChild(); child != null;
             child = child.getNext()) {
          if (child.isGetterDef() || child.isSetterDef()) {
            // ES5 get/set not supported.
            return false;
          }

          Node childVal = child.getFirstChild();
          // Check if childVal is the parent of any of the passed in
          // references, as that is how self‑referential assignments
          // will happen.
          for (Reference t : refs) {
            Node refNode = t.getParent();
            while (!NodeUtil.isStatementBlock(refNode)) {
              if (refNode == childVal) {
                // There's a self‑referential assignment
                return false;
              }
              refNode = refNode.getParent();
            }
          }
        }

        // Collect property names from this object literal.
        for (Node child = val.getFirstChild(); child != null;
             child = child.getNext()) {
          if (child.isStringKey()) {
            propNames.add(child.getString());
          }
        }

        hasAssignment = true;
      }

      if (!hasAssignment) {
        return false;
      }

      // Second pass: check GETPROP references.
      for (Reference ref : refs) {
        Node parent = ref.getParent();
        Node gramps = ref.getGrandparent();

        if (parent.isGetProp()) {
          Preconditions.checkState(parent.getFirstChild() == ref.getNode());
          // A call target maybe using the object as a 'this' value.
          if (gramps.isCall() && gramps.getFirstChild() == parent) {
            return false;
          }

          // Bail out if we see a reference to a property that isn't defined on
          // any of the collected object literals.
          String propName = parent.getSecondChild().getString();
          if (!propNames.contains(propName)) {
            return false;
          }
        }
      }

      return true;
    }