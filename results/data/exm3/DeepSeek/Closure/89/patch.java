  private void updateSimpleDeclaration(String alias, Name refName, Ref ref) {
    Node rvalue = ref.node.getNext();
    Node parent = ref.node.getParent();
    Node gramps = parent.getParent();
    Node greatGramps = gramps.getParent();
    Node greatGreatGramps = greatGramps.getParent();


    // Create the new alias node.
    Node nameNode = NodeUtil.newName(
        compiler.getCodingConvention(), alias, gramps.getFirstChild(),
        refName.fullName());
    NodeUtil.copyNameAnnotations(ref.node.getLastChild(), nameNode);

    if (gramps.getType() == Token.EXPR_RESULT) {
      // BEFORE: a.b.c = ...;
      //   exprstmt
      //     assign
      //       getprop
      //         getprop
      //           name a
      //           string b
      //         string c
      //       NODE
      // AFTER: var a$b$c = ...;
      //   var
      //     name a$b$c
      //       NODE

      // Remove the rvalue (NODE).
      parent.removeChild(rvalue);
      nameNode.addChildToFront(rvalue);

      Node varNode = new Node(Token.VAR, nameNode);
      greatGramps.replaceChild(gramps, varNode);
    } else {
      // This must be a complex assignment.
      Preconditions.checkNotNull(ref.getTwin());

      // BEFORE:
      // ... (x.y = 3);
      //
      // AFTER:
      // var x$y;
      // ... (x$y = 3);

      Node current = gramps;
      Node currentParent = gramps.getParent();
      for (; currentParent.getType() != Token.SCRIPT &&
             currentParent.getType() != Token.BLOCK;
           current = currentParent,
           currentParent = currentParent.getParent()) {}

      // Check if the alias is already declared in the current block.
      boolean aliasAlreadyDeclared = false;
      for (Node child = currentParent.getFirstChild(); child != null; child = child.getNext()) {
        if (child.getType() == Token.VAR) {
          for (Node varChild = child.getFirstChild(); varChild != null; varChild = varChild.getNext()) {
            if (varChild.getType() == Token.NAME && varChild.getString().equals(alias)) {
              aliasAlreadyDeclared = true;
              break;
            }
          }
        }
        if (aliasAlreadyDeclared) break;
      }

      // Create a stub variable declaration only if not already declared.
      if (!aliasAlreadyDeclared) {
        Node clonedName = nameNode.cloneTree();
        NodeUtil.copyNameAnnotations(nameNode, clonedName);
        Node stubVar = new Node(Token.VAR, clonedName)
            .copyInformationFrom(nameNode);
        currentParent.addChildBefore(stubVar, current);
      }

      parent.replaceChild(ref.node, nameNode);
    }

    compiler.reportCodeChange();
  }