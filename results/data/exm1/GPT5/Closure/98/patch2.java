BasicBlock(BasicBlock parent, Node root) {
      this.parent = parent;
      this.root = root;

      // only named functions may be hoisted.
      this.isHoisted = NodeUtil.isHoistedFunctionDeclaration(root);
    }