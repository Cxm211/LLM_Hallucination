private void updateSimpleDeclaration(String alias, Name refName, Ref ref) {
    Node rvalue = ref.node.getNext();
    Node parent = ref.node.getParent();
    Node gramps = parent.getParent();
    Node greatGramps = gramps.getParent();
    Node greatGreatGramps = greatGramps.getParent();


    Node nameNode = NodeUtil.newName(
        compiler.getCodingConvention(), alias, gramps.getFirstChild(),
        refName.fullName());
    NodeUtil.copyNameAnnotations(ref.node.getLastChild(), nameNode);

    if (gramps.getType() == Token.EXPR_RESULT) {
      parent.removeChild(rvalue);
      nameNode.addChildToFront(rvalue);

      Node varNode = new Node(Token.VAR, nameNode);
      greatGramps.replaceChild(gramps, varNode);
    } else {
      Preconditions.checkNotNull(ref.getTwin());

      Node current = gramps;
      Node currentParent = gramps.getParent();
      for (; currentParent.getType() != Token.SCRIPT &&
             currentParent.getType() != Token.BLOCK;
           current = currentParent,
           currentParent = currentParent.getParent()) {}

      Node stubVar = new Node(Token.VAR, nameNode.cloneTree())
          .copyInformationFrom(nameNode);
      currentParent.addChildBefore(stubVar, current);

      parent.replaceChild(ref.node, nameNode);
    }

    compiler.reportCodeChange();
  }