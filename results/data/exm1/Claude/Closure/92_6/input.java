// buggy code
    void replace() {
      if (firstNode == null) {
        // Don't touch the base case ('goog').
        replacementNode = candidateDefinition;
        return;
      }

      // Handle the case where there is a duplicate definition for an explicitly
      // provided symbol.
      if (candidateDefinition != null && explicitNode != null) {
        explicitNode.detachFromParent();
        compiler.reportCodeChange();

        // Does this need a VAR keyword?
        replacementNode = candidateDefinition;
        if (NodeUtil.isExpressionNode(candidateDefinition)) {
          candidateDefinition.putBooleanProp(Node.IS_NAMESPACE, true);
          Node assignNode = candidateDefinition.getFirstChild();
          Node nameNode = assignNode.getFirstChild();
          if (nameNode.getType() == Token.NAME) {
            // Need to convert this assign to a var declaration.
            Node valueNode = nameNode.getNext();
            assignNode.removeChild(nameNode);
            assignNode.removeChild(valueNode);
            nameNode.addChildToFront(valueNode);
            Node varNode = new Node(Token.VAR, nameNode);
            varNode.copyInformationFrom(candidateDefinition);
            candidateDefinition.getParent().replaceChild(
                candidateDefinition, varNode);
            nameNode.setJSDocInfo(assignNode.getJSDocInfo());
            compiler.reportCodeChange();
            replacementNode = varNode;
          }
        }
      } else {
        // Handle the case where there's not a duplicate definition.
        replacementNode = createDeclarationNode();
        if (firstModule == minimumModule) {
          firstNode.getParent().addChildBefore(replacementNode, firstNode);
        } else {
          // In this case, the name was implicitly provided by two independent
          // modules. We need to move this code up to a common module.
          int indexOfDot = namespace.indexOf('.');
          if (indexOfDot == -1) {
            // Any old place is fine.
            compiler.getNodeForCodeInsertion(minimumModule)
                .addChildToBack(replacementNode);
          } else {
            // Add it after the parent namespace.
            ProvidedName parentName =
                providedNames.get(namespace.substring(0, indexOfDot));
            Preconditions.checkNotNull(parentName);
            Preconditions.checkNotNull(parentName.replacementNode);
            parentName.replacementNode.getParent().addChildAfter(
                replacementNode, parentName.replacementNode);
          }
        }
        if (explicitNode != null) {
          explicitNode.detachFromParent();
        }
        compiler.reportCodeChange();
      }
    }

// relevant test
// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType2
  public void testBadTemplateType2() throws Exception {
    testTypes(
        "\n" +
        "function f(x, y) {}\n" +
        "f(0, function() {});",
        TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType3
  public void testBadTemplateType3() throws Exception {
    testTypes(
        "\n" +
        "function f(x) {}\n" +
        "f(this);",
        TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType4
  public void testBadTemplateType4() throws Exception {
    testTypes(
        "\n" +
        "function f() {}\n" +
        "f();",
        FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadTemplateType5
  public void testBadTemplateType5() throws Exception {
    testTypes(
        "\n" +
        "function f() {}\n" +
        "f();",
        FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.format(), true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralUndefinedThisArgument
  public void testFunctionLiteralUndefinedThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() { this; });",
        "Function literal argument refers to undefined this argument");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionLiteralDefinedThisArgument
  public void testFunctionLiteralDefinedThisArgument() throws Exception {
    testTypes(""
        + "\n"
        + "function baz(fn, opt_obj) {}\n"
        + "baz(function() { this; }, {});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testActiveXObject
  public void testActiveXObject() throws Exception {
    testTypes(
        " var x = new ActiveXObject();" +
        " var y = new ActiveXObject();");
  }
