    private void handleObjectLit(NodeTraversal t, Node n) {
      for (Node child = n.getFirstChild();
          child != null;
          child = child.getNext()) {
        // Maybe STRING, GET, SET, or NUMBER (numeric keys). Skip numeric keys.
        if (child.getType() == Token.NUMBER) {
          // Numeric property keys are not considered for renaming/processing here.
          continue;
        }

        String name = child.getString();
        // In some AST shapes for getters/setters, the name may be on the first child.
        if ((name == null || name.isEmpty()) && child.getFirstChild() != null && child.getFirstChild().isString()) {
          name = child.getFirstChild().getString();
        }
        if (name == null) {
          continue;
        }

        T type = typeSystem.getType(getScope(), n, name);

        Property prop = getProperty(name);
        if (!prop.scheduleRenaming(child,
                                   processProperty(t, prop, type, null))) {
          // TODO(user): It doesn't look like the user can do much in this
          // case right now.
          if (propertiesToErrorFor.containsKey(name)) {
            compiler.report(JSError.make(
                t.getSourceName(), child, propertiesToErrorFor.get(name),
                Warnings.INVALIDATION, name,
                (type == null ? "null" : type.toString()), n.toString(), ""));
          }
        }
      }
    }