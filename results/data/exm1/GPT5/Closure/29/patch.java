    private boolean isInlinableObject(List<Reference> refs) {
      // First, collect all property names defined on any object literal assignments
      // to this reference. If we later see a GETPROP that refers to a property not
      // in this set, we should bail out (see note about Object.prototype blind spot).
      Set<String> definedProps = new HashSet<String>();
      for (Reference r : refs) {
        Node name = r.getNode();
        if (!isVarOrAssignExprLhs(name)) {
          continue;
        }
        Node val = r.getAssignedValue();
        if (val != null && val.isObjectLit()) {
          for (Node child = val.getFirstChild(); child != null; child = child.getNext()) {
            if (!(child.isGetterDef() || child.isSetterDef())) {
              definedProps.add(child.getString());
            }
          }
        }
      }

      boolean ret = false;
      for (Reference ref : refs) {
        Node name = ref.getNode();
        Node parent = ref.getParent();
        Node gramps = ref.getGrandparent();

        // Ignore indirect references, like x.y (except x.y(), since
        // the function referenced by y might reference 'this').
        //
        if (parent.isGetProp()) {
          Preconditions.checkState(parent.getFirstChild() == name);
          // A call target maybe using the object as a 'this' value.
          if (gramps.isCall()
              && gramps.getFirstChild() == parent) {
            return false;
          }

          // See NOTE(nicksantos) below: bail out if we see a reference
          // to a property that isn't defined on the object literal.
          String propName = parent.getLastChild().getString();
          if (!definedProps.contains(propName)) {
            return false;
          }
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

        // Make sure that the value is not self-refential. IOW,
        // disallow things like x = {b: x.a}.
        //
        // TODO: Only exclude unorderable self-referential
        // assignments. i.e. x = {a: x.b, b: x.a} is not orderable,
        // but x = {a: 1, b: x.a} is.
        //
        // Also, ES5 getters/setters aren't handled by this pass.
        for (Node child = val.getFirstChild(); child != null;
             child = child.getNext()) {
          if (child.isGetterDef() ||
              child.isSetterDef()) {
            // ES5 get/set not supported.
            return false;
          }


          Node childVal = child.getFirstChild();
          // Check if childVal is the parent of any of the passed in
          // references, as that is how self-referential assignments
          // will happen.
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


        // We have found an acceptable object literal assignment. As
        // long as there are no other assignments that mess things up,
        // we can inline.
        ret = true;
      }
      return ret;
    }