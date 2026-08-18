private boolean isInlinableObject(List<Reference> refs) {
      // First, collect all property names defined on object literal assignments
      // and validate the object literal assignments themselves.
      Set<String> definedProps = new HashSet<>();
      boolean hasObjectLitAssignment = false;

      for (Reference ref : refs) {
        Node name = ref.getNode();
        Node parent = ref.getParent();

        // Only rewrite VAR declarations or simple assignment statements
        if (!parent.isGetProp()) {
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

          // Validate object literal contents; also collect defined properties.
          for (Node child = val.getFirstChild(); child != null; child = child.getNext()) {
            if (child.isGetterDef() || child.isSetterDef()) {
              // ES5 get/set not supported.
              return false;
            }
            String propName = child.getString();
            if (propName != null) {
              definedProps.add(propName);
            }

            Node childVal = child.getFirstChild();
            // Check if childVal is the parent of any of the passed in
            // references, as that is how self-referential assignments will happen.
            for (Reference t : refs) {
              Node refNode = t.getParent();
              while (!NodeUtil.isStatementBlock(refNode)) {
                if (refNode == childVal) {
                  // There's a self-referential assignment
                  return false;
                }
                refNode = refNode.getParent();
              }
            }
          }

          hasObjectLitAssignment = true;
        }
      }

      // Second pass: validate indirect property references against defined properties.
      for (Reference ref : refs) {
        Node name = ref.getNode();
        Node parent = ref.getParent();
        Node gramps = ref.getGrandparent();

        if (parent.isGetProp()) {
          Preconditions.checkState(parent.getFirstChild() == name);
          // A call target may be using the object as a 'this' value.
          if (gramps.isCall() && gramps.getFirstChild() == parent) {
            return false;
          }

          // Bail out if a referenced property isn't defined on the object literal(s).
          Node prop = parent.getLastChild();
          String propName = prop != null ? prop.getString() : null;
          if (propName == null || !definedProps.contains(propName)) {
            return false;
          }
        }
      }

      return hasObjectLitAssignment;
    }