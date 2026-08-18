    private void visitLabel(Node node, Node parent) {
      Node nameNode = node.getFirstChild();
      Preconditions.checkState(nameNode != null);
      String name = nameNode.getString();
      LabelInfo li = getLabelInfo(name);
      // This is a label...
      String newName = getNameForId(li.id);
      boolean renamed = false;
      if (!name.equals(newName)) {
        // Give it the new name.
        nameNode.setString(newName);
        compiler.reportCodeChange();
        renamed = true;
      }
      if (!li.referenced && !renamed) {
        // ... and it is not referenced and not renamed, just remove it.
        Node newChild = node.getLastChild();
        node.removeChild(newChild);
        parent.replaceChild(node, newChild);
        if (newChild.getType() == Token.BLOCK) {
          NodeUtil.tryMergeBlock(newChild);
        }
        compiler.reportCodeChange();
      }
      // Do not remove the mapping; it is needed for references.
    }