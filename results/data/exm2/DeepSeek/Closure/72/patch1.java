    private void visitLabel(Node node, Node parent) {
      Node nameNode = node.getFirstChild();
      Preconditions.checkState(nameNode != null);
      String name = nameNode.getString();
      LabelInfo li = getLabelInfo(name);
      // This is a label...
      String newName = getNameForId(li.id);
      if (li.referenced || !name.equals(newName)) {
        // Rename if referenced or if the name changes (conflict)
        if (!name.equals(newName)) {
          // ... and it is used, give it the short name.
          nameNode.setString(newName);
          compiler.reportCodeChange();
        }
      } else {
        // ... and it is not referenced and no name change, just remove it.
        Node newChild = node.getLastChild();
        node.removeChild(newChild);
        parent.replaceChild(node, newChild);
        if (newChild.getType() == Token.BLOCK) {
          NodeUtil.tryMergeBlock(newChild);
        }
        compiler.reportCodeChange();
      }

      // Remove the label from the current stack of labels.
      namespaceStack.peek().renameMap.remove(name);
    }