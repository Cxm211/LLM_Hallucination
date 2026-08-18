private void makeLocalNamesUnique(Node fnNode, boolean isCallInLoop) {
    Supplier<String> idSupplier = compiler.getUniqueNameIdSupplier();
    // Make variable names unique to this instance.
    NodeTraversal.traverse(
        compiler,
        fnNode,
        new MakeDeclaredNamesUnique(
            new InlineRenamer(
                idSupplier,
                "inline_",
                isCallInLoop)));
    // Make label names unique to this instance and update all references.
    final java.util.Map<String, String> labelMap = new java.util.HashMap<>();
    NodeTraversal.traverse(
        compiler,
        fnNode,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            int type = n.getType();
            if (type == Token.LABEL) {
              Node nameNode = n.getFirstChild();
              if (nameNode != null) {
                String oldName = nameNode.getString();
                String newName = labelMap.get(oldName);
                if (newName == null) {
                  newName = "JSCompiler_inline_label_" + idSupplier.get();
                  labelMap.put(oldName, newName);
                }
                if (!oldName.equals(newName)) {
                  nameNode.setString(newName);
                  compiler.reportCodeChange();
                }
              }
            } else if (type == Token.BREAK || type == Token.CONTINUE) {
              Node nameNode = n.getFirstChild();
              if (nameNode != null) {
                String oldName = nameNode.getString();
                String newName = labelMap.get(oldName);
                if (newName != null && !oldName.equals(newName)) {
                  nameNode.setString(newName);
                  compiler.reportCodeChange();
                }
              }
            }
          }
        });
  }