private void handleObjectLit(NodeTraversal t, Node n) {
      for (Node child = n.getFirstChild();
          child != null;
          child = child.getNext()) {
        String name = child.getString();
        T type = typeSystem.getType(getScope(), child, name);
        Property prop = getProperty(name);
        if (prop != null && !prop.scheduleRenaming(child,
                                   processProperty(t, prop, type, null))) {
          if (propertiesToErrorFor.containsKey(name)) {
            compiler.report(JSError.make(
                t.getSourceName(), child, propertiesToErrorFor.get(name),
                Warnings.INVALIDATION, name,
                (type == null ? "null" : type.toString()), n.toString(), ""));
          }
        }
      }
    }