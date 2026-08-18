    public void applyAlias() {
      aliasReference.getParent().replaceChild(
          aliasReference, aliasDefinition.cloneTree());
    }

    AliasedTypeNode(Node typeReference,
        String aliasName) {
      this.typeReference = typeReference;
      this.aliasName = aliasName;
    }

    public void applyAlias() {
      typeReference.setString(aliasName);
    }

    private void fixTypeNode(Node typeNode) {
      if (typeNode.isString()) {
        String name = typeNode.getString();
        int endIndex = name.indexOf('.');
        if (endIndex == -1) {
          endIndex = name.length();
        }
        String baseName = name.substring(0, endIndex);
        Var aliasVar = aliases.get(baseName);
        if (aliasVar != null) {
          Node aliasedNode = aliasVar.getInitialValue();
          aliasUsages.add(new AliasedTypeNode(typeNode, aliasedNode.getQualifiedName() + name.substring(endIndex)));
        }
      }

      for (Node child = typeNode.getFirstChild(); child != null;
           child = child.getNext()) {
        fixTypeNode(child);
      }
    }

// trigger testcase
public void testIssue772() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.closurePass = true;
    options.checkTypes = true;
    test(
        options,
        "/** @const */ var a = {};" +
        "/** @const */ a.b = {};" +
        "/** @const */ a.b.c = {};" +
        "goog.scope(function() {" +
        "  var b = a.b;" +
        "  var c = b.c;" +
        "  /** @typedef {string} */" +
        "  c.MyType;" +
        "  /** @param {c.MyType} x The variable. */" +
        "  c.myFunc = function(x) {};" +
        "});",
        "/** @const */ var a = {};" +
        "/** @const */ a.b = {};" +
        "/** @const */ a.b.c = {};" +
        "a.b.c.MyType;" +
        "a.b.c.myFunc = function(x) {};");
  }

public void testIssue772() {
    testTypes(
        "var b = a.b;" +
        "var c = b.c;",
        "/** @param {c.MyType} x */ types.actual;" +
        "/** @param {a.b.c.MyType} x */ types.expected;");
  }
