    private void handleObjectLit(NodeTraversal t, Node n) {
      for (Node child = n.getFirstChild();
          child != null;
          child = child.getNext()) {
        // Maybe STRING, GET, SET

        // We should never see a mix of numbers and strings.
        Node keyNode = child;
        int typeToken = child.getType();

        // Skip numeric keys entirely; they should not be renamed.
        if (typeToken == Token.NUMBER) {
          continue;
        }

        // For getter/setter definitions, the key is the first child.
        if (typeToken == Token.GET || typeToken == Token.SET) {
          keyNode = child.getFirstChild();
          if (keyNode == null) {
            continue;
          }
          // If the getter/setter name is numeric, skip renaming.
          if (keyNode.getType() == Token.NUMBER) {
            continue;
          }
        }

        String name = keyNode.getString();
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