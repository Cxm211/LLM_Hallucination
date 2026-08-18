    private void handleObjectLit(NodeTraversal t, Node n) {
      for (Node child = n.getFirstChild();
          child != null;
          child = child.getNext()) {
        // Maybe STRING, GET, SET, or NUMBER

        // We should never see a mix of numbers and strings.
        String name = child.getString();
        if ((name == null || name.isEmpty()) && child.getType() == Token.NUMBER) {
          double num = child.getDouble();
          long asLong = (long) num;
          name = (num == asLong) ? Long.toString(asLong) : Double.toString(num);
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