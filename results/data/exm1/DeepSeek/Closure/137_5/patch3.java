    public void visit(NodeTraversal t, Node node, Node parent) {
      if (node.getType() == Token.NAME) {
        String oldName = node.getString();
        if (containsSeparator(oldName)) {
          Scope scope = t.getScope();
          Var var = t.getScope().getVar(oldName);
          if (var == null || var.isGlobal()) {
        return;
      }
          // Avoid renaming catch variables
          if (var.getParentNode() != null && var.getParentNode().getType() == Token.CATCH) {
            nameMap.put(var, oldName);
            return;
          }

          if (nameMap.containsKey(var)) {
            node.setString(nameMap.get(var));
          } else {
            int index = indexOfSeparator(oldName);
            String newName = getOrginalNameInternal(oldName, index);
            String suffix = getNameSuffix(oldName, index);

      // Merge any names that were referenced but not declared in the current
      // scope.
      // If there isn't anything left in the stack we will be going into the
      // global scope: don't try to build a set of referenced names for the
      // global scope.
            boolean recurseScopes = false;
            if (!suffix.matches("\\d+")) {
              recurseScopes = true;
            }

    /**
     * For the Var declared in the current scope determine if it is possible
     * to revert the name to its orginal form without conflicting with other
     * values.
     */
        // Check if the new name is valid and if it would cause conflicts.
            if (var.scope.isDeclared(newName, recurseScopes) ||
                !TokenStream.isJSIdentifier(newName)) {
              newName = oldName;
            } else {
              var.scope.declare(newName, var.nameNode, null, null);
          // Adding a reference to the new name to prevent either the parent
          // scopes or the current scope renaming another var to this new name.
              Node parentNode = var.getParentNode();
              if (parentNode.getType() == Token.FUNCTION &&
                  parentNode == var.scope.getRootNode()) {
                var.getNameNode().setString(newName);
              }
              node.setString(newName);
          compiler.reportCodeChange();
        }

            nameMap.put(var, newName);

      }

        // Add all referenced names to the set so it is possible to check for
        // conflicts.
        // Store only references to candidate names in the node map.
        }
      }
    }