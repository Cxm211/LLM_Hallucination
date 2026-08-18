    private void visitLabel(Node node, Node parent) {
      Node nameNode = node.getFirstChild();
      Preconditions.checkState(nameNode != null);
      String name = nameNode.getString();
      LabelInfo li = getLabelInfo(name);
      // This is a label...
      if (li.referenced) {
        String newName = getNameForId(li.id);
        if (!name.equals(newName)) {
          // ... and it is used, give it the short name.
          nameNode.setString(newName);
          compiler.reportCodeChange();
          // Update the rename map with the new name.
          namespaceStack.peek().renameMap.remove(name);
          namespaceStack.peek().renameMap.put(newName, li);
        } else {
          // Remove the name from rename map.
          namespaceStack.peek().renameMap.remove(name);
        }
      } else {
        // ... and it is not referenced, just remove it.
        Node newChild = node.getLastChild();
        node.removeChild(newChild);
        parent.replaceChild(node, newChild);
        if (newChild.getType() == Token.BLOCK) {
          NodeUtil.tryMergeBlock(newChild);
        }
        compiler.reportCodeChange();
        namespaceStack.peek().renameMap.remove(name);
      }
    }