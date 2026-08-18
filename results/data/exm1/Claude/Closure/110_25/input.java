// buggy code
    private void findAliases(NodeTraversal t) {
      Scope scope = t.getScope();
      for (Var v : scope.getVarIterable()) {
        Node n = v.getNode();
        Node parent = n.getParent();
        boolean isVar = parent.isVar();
        if (isVar && n.getFirstChild() != null && n.getFirstChild().isQualifiedName()) {
          recordAlias(v);
        } else if (v.isBleedingFunction()) {
          // Bleeding functions already get a BAD_PARAMETERS error, so just
          // do nothing.
        } else if (parent.getType() == Token.LP) {
          // Parameters of the scope function also get a BAD_PARAMETERS
          // error.
        } else if (isVar) {
          Node grandparent = parent.getParent();
          Node value = n.hasChildren() ?
              v.getInitialValue().detachFromParent() :
              null;
          Node varNode = parent;

          String name = n.getString();
          int nameCount = scopedAliasNames.count(name);
          scopedAliasNames.add(name);
          String globalName =
              "$jscomp.scope." + name + (nameCount == 0 ? "" : ("$" + nameCount));

          compiler.ensureLibraryInjected("base");

          // First, we need to free up the function expression (EXPR)
          // to be used in another expression.
            // Replace "function NAME() { ... }" with "var NAME;".

            // We can't keep the local name on the function expression,
            // because IE is buggy and will leak the name into the global
            // scope. This is covered in more detail here:
            // http://wiki.ecmascript.org/lib/exe/fetch.php?id=resources:resources&cache=cache&media=resources:jscriptdeviationsfromes3.pdf
            //
            // This will only cause problems if this is a hoisted, recursive
            // function, and the programmer is using the hoisting.

              // If this is a VAR, we can just detach the expression and
              // the tree will still be valid.

          // Add $jscomp.scope.name = EXPR;
          // Make sure we copy over all the jsdoc and debug info.
          if (value != null || v.getJSDocInfo() != null) {
            Node newDecl = NodeUtil.newQualifiedNameNodeDeclaration(
                compiler.getCodingConvention(),
                globalName,
                value,
                v.getJSDocInfo())
                .useSourceInfoIfMissingFromForTree(n);
            NodeUtil.setDebugInformation(
                newDecl.getFirstChild().getFirstChild(), n, name);

              grandparent.addChildBefore(newDecl, varNode);
          }

          // Rewrite "var name = EXPR;" to "var name = $jscomp.scope.name;"
          v.getNameNode().addChildToFront(
              NodeUtil.newQualifiedNameNode(
                  compiler.getCodingConvention(), globalName, n, name));

          recordAlias(v);
        } else {
          // Do not other kinds of local symbols, like catch params.
          report(t, n, GOOG_SCOPE_NON_ALIAS_LOCAL, n.getString());
        }
      }
    }

  public Node getChildBefore(Node child) {
    if (child == first) {
      return null;
    }
    Node n = first;

    while (n.next != child) {
      n = n.next;
      if (n == null) {
        throw new RuntimeException("node is not a child");
      }
    }
    return n;
  }

// relevant test
// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression15
  public void testRegression15() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @desc Hello,\n" +
        " * World!\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertEquals("Hello, World!", jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
    assertFalse(jsdoc.isExport());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression16
  public void testRegression16() throws Exception {
    JSDocInfo jsdoc = parse(
        " Email is plp@foo.bar\n" +
        " @type {string}\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertTypeEquals(STRING_TYPE, jsdoc.getType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression17
  public void testRegression17() throws Exception {
    
    assertNull(parse("@private*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression1
  public void testFullRegression1() throws Exception {
    parseFull("function bar(foo){}",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression2
  public void testFullRegression2() throws Exception {
    parseFull("function bar(foo){}",
        "Bad type annotation. expected closing }",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression3
  public void testFullRegression3() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug907488
  public void testBug907488() throws Exception {
    parse("@type {number,null} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug907494
  public void testBug907494() throws Exception {
    parse("@return {Object,undefined} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug909468
  public void testBug909468() throws Exception {
    parse("@extends {(x)}*/",
        "Bad type annotation. expecting a type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInterface
  public void testParseInterface() throws Exception {
    assertTrue(parse("@interface*/").isInterface());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplicitCast1
  public void testParseImplicitCast1() throws Exception {
    assertTrue(parse("@type {string} \n * @implicitCast*/").isImplicitCast());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplicitCast2
  public void testParseImplicitCast2() throws Exception {
    assertFalse(parse("@type {string}*/").isImplicitCast());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDuplicateImplicitCast
  public void testParseDuplicateImplicitCast() throws Exception {
    parse("@type {string} \n * @implicitCast \n * @implicitCast*/",
          "Bad type annotation. extra @implicitCast tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInterfaceDoubled
  public void testParseInterfaceDoubled() throws Exception {
    parse(
        "* @interface\n" +
        "* @interface\n" +
        "*/",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplements
  public void testParseImplements() throws Exception {
    List<JSTypeExpression> interfaces = parse("@implements {SomeInterface}*/")
        .getImplementedInterfaces();
    assertEquals(1, interfaces.size());
    assertTypeEquals(registry.createNamedType("SomeInterface", null, -1, -1),
        interfaces.get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsTwo
  public void testParseImplementsTwo() throws Exception {
    List<JSTypeExpression> interfaces =
        parse(
            "* @implements {SomeInterface1}\n" +
            "* @implements {SomeInterface2}\n" +
            "*/")
        .getImplementedInterfaces();
    assertEquals(2, interfaces.size());
    assertTypeEquals(registry.createNamedType("SomeInterface1", null, -1, -1),
        interfaces.get(0));
    assertTypeEquals(registry.createNamedType("SomeInterface2", null, -1, -1),
        interfaces.get(1));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsSameTwice
  public void testParseImplementsSameTwice() throws Exception {
    parse(
        "* @implements {Smth}\n" +
        "* @implements {Smth}\n" +
        "*/",
        "Bad type annotation. duplicate @implements tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNoName
  public void testParseImplementsNoName() throws Exception {
    parse("* @implements {} */",
        "Bad type annotation. expecting a type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsMissingRC
  public void testParseImplementsMissingRC() throws Exception {
    parse("* @implements {Smth */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNullable1
  public void testParseImplementsNullable1() throws Exception {
    parse("@implements {Base?} */", "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNullable2
  public void testParseImplementsNullable2() throws Exception {
    parse("@implements Base? */",
        "Bad type annotation. expected end of line or comment");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testInterfaceExtends
  public void testInterfaceExtends() throws Exception {
     JSDocInfo jsdoc = parse(
         " * @interface \n" +
         " * @extends {Extended} */");
    assertTrue(jsdoc.isInterface());
    assertEquals(1, jsdoc.getExtendedInterfacesCount());
    List<JSTypeExpression> types = jsdoc.getExtendedInterfaces();
    assertTypeEquals(registry.createNamedType("Extended", null, -1, -1),
        types.get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testInterfaceMultiExtends1
  public void testInterfaceMultiExtends1() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @interface \n" +
        " * @extends {Extended1} \n" +
        " * @extends {Extended2} */");
    assertTrue(jsdoc.isInterface());
    assertNull(jsdoc.getBaseType());
    assertEquals(2, jsdoc.getExtendedInterfacesCount());
    List<JSTypeExpression> types = jsdoc.getExtendedInterfaces();
    assertTypeEquals(registry.createNamedType("Extended1", null, -1, -1),
       types.get(0));
    assertTypeEquals(registry.createNamedType("Extended2", null, -1, -1),
        types.get(1));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testInterfaceMultiExtends2
  public void testInterfaceMultiExtends2() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @extends {Extended1} \n" +
        " * @interface \n" +
        " * @extends {Extended2} \n" +
        " * @extends {Extended3} */");
    assertTrue(jsdoc.isInterface());
    assertNull(jsdoc.getBaseType());
    assertEquals(3, jsdoc.getExtendedInterfacesCount());
    List<JSTypeExpression> types = jsdoc.getExtendedInterfaces();
    assertTypeEquals(registry.createNamedType("Extended1", null, -1, -1),
       types.get(0));
    assertTypeEquals(registry.createNamedType("Extended2", null, -1, -1),
        types.get(1));
    assertTypeEquals(registry.createNamedType("Extended3", null, -1, -1),
        types.get(2));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadClassMultiExtends
  public void testBadClassMultiExtends() throws Exception {
    parse(" * @extends {Extended1} \n" +
        " * @constructor \n" +
        " * @extends {Extended2} */",
        "Bad type annotation. type annotation incompatible with other " +
        "annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadExtendsWithNullable
  public void testBadExtendsWithNullable() throws Exception {
    JSDocInfo jsdoc = parse("@constructor\n * @extends {Object?} */",
        "Bad type annotation. expected closing }");
    assertTrue(jsdoc.isConstructor());
    assertTypeEquals(OBJECT_TYPE, jsdoc.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadImplementsWithNullable
  public void testBadImplementsWithNullable() throws Exception {
  JSDocInfo jsdoc = parse("@implements {Disposable?}\n * @constructor */",
      "Bad type annotation. expected closing }");
    assertTrue(jsdoc.isConstructor());
    assertTypeEquals(registry.createNamedType("Disposable", null, -1, -1),
        jsdoc.getImplementedInterfaces().get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadTypeDefInterfaceAndConstructor1
  public void testBadTypeDefInterfaceAndConstructor1() throws Exception {
    JSDocInfo jsdoc = parse("@interface\n@constructor*/",
        "Bad type annotation. cannot be both an interface and a constructor");
    assertTrue(jsdoc.isInterface());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadTypeDefInterfaceAndConstructor2
  public void testBadTypeDefInterfaceAndConstructor2() throws Exception {
    JSDocInfo jsdoc = parse("@constructor\n@interface*/",
        "Bad type annotation. cannot be both an interface and a constructor");
    assertTrue(jsdoc.isConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationParameter
  public void testDocumentationParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description.*/", true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description.",
                 jsdoc.getDescriptionForParameter("number42"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testMultilineDocumentationParameter
  public void testMultilineDocumentationParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description"
                + "\n* on multiple \n* lines.*/", true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description on multiple lines.",
                 jsdoc.getDescriptionForParameter("number42"));

  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationMultipleParameter
  public void testDocumentationMultipleParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description."
                + "\n* @param {Integer} number87 This is another description.*/"
                , true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description.",
                 jsdoc.getDescriptionForParameter("number42"));

    assertTrue(jsdoc.hasDescriptionForParameter("number87"));
    assertEquals("This is another description.",
                 jsdoc.getDescriptionForParameter("number87"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationMultipleParameter2
  public void testDocumentationMultipleParameter2() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {number} delta = 0 results in a redraw\n" +
                "  != 0 ..... */", true);
    assertTrue(jsdoc.hasDescriptionForParameter("delta"));
    assertEquals("= 0 results in a redraw != 0 .....",
                 jsdoc.getDescriptionForParameter("delta"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testAuthors
  public void testAuthors() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description."
                + "\n* @param {Integer} number87 This is another description."
                + "\n* @author a@google.com (A Person)"
                + "\n* @author b@google.com (B Person)"
                + "\n* @author c@google.com (C Person)*/"
                , true);

    Collection<String> authors = jsdoc.getAuthors();

    assertTrue(authors != null);
    assertTrue(authors.size() == 3);

    assertContains(authors, "a@google.com (A Person)");
    assertContains(authors, "b@google.com (B Person)");
    assertContains(authors, "c@google.com (C Person)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSuppress1
  public void testSuppress1() throws Exception {
    JSDocInfo info = parse("@suppress {x} */");
    assertEquals(Sets.newHashSet("x"), info.getSuppressions());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSuppress2
  public void testSuppress2() throws Exception {
    JSDocInfo info = parse("@suppress {x|y|x|z} */");
    assertEquals(Sets.newHashSet("x", "y", "z"), info.getSuppressions());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSuppress3
  public void testSuppress3() throws Exception {
    JSDocInfo info = parse("@suppress {x,y} */");
    assertEquals(Sets.newHashSet("x", "y"), info.getSuppressions());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress1
  public void testBadSuppress1() throws Exception {
    parse("@suppress {} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress2
  public void testBadSuppress2() throws Exception {
    parse("@suppress {x|} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress3
  public void testBadSuppress3() throws Exception {
    parse("@suppress {|x} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress4
  public void testBadSuppress4() throws Exception {
    parse("@suppress {x|y */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress6
  public void testBadSuppress6() throws Exception {
    parse("@suppress {x} \n * @suppress {y} */", "duplicate @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress7
  public void testBadSuppress7() throws Exception {
    parse("@suppress {impossible} */",
          "unknown @suppress parameter: impossible");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies1
  public void testModifies1() throws Exception {
    JSDocInfo info = parse("@modifies {this} */");
    assertEquals(Sets.newHashSet("this"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies2
  public void testModifies2() throws Exception {
    JSDocInfo info = parse("@modifies {arguments} */");
    assertEquals(Sets.newHashSet("arguments"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies3
  public void testModifies3() throws Exception {
    JSDocInfo info = parse("@modifies {this|arguments} */");
    assertEquals(Sets.newHashSet("this", "arguments"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies4
  public void testModifies4() throws Exception {
    JSDocInfo info = parse("@param {*} x\n * @modifies {x} */");
    assertEquals(Sets.newHashSet("x"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies5
  public void testModifies5() throws Exception {
    JSDocInfo info = parse(
        "@param {*} x\n"
        + " * @param {*} y\n"
        + " * @modifies {x} */");
    assertEquals(Sets.newHashSet("x"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies6
  public void testModifies6() throws Exception {
    JSDocInfo info = parse(
        "@param {*} x\n"
        + " * @param {*} y\n"
        + " * @modifies {x|y} */");
    assertEquals(Sets.newHashSet("x", "y"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies1
  public void testBadModifies1() throws Exception {
    parse("@modifies {} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies2
  public void testBadModifies2() throws Exception {
    parse("@modifies {this|} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies3
  public void testBadModifies3() throws Exception {
    parse("@modifies {|this} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies4
  public void testBadModifies4() throws Exception {
    parse("@modifies {this|arguments */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies5
  public void testBadModifies5() throws Exception {
    parse("@modifies {this,arguments} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies6
  public void testBadModifies6() throws Exception {
    parse("@modifies {this} \n * @modifies {this} */",
        "conflicting @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies7
  public void testBadModifies7() throws Exception {
    parse("@modifies {impossible} */",
          "unknown @modifies parameter: impossible");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies8
  public void testBadModifies8() throws Exception {
    parse("@modifies {this}\n"
        + "@nosideeffects */", "conflicting @nosideeffects tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies9
  public void testBadModifies9() throws Exception {
    parse("@nosideeffects\n"
        + "@modifies {this} */", "conflicting @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewSingleLine
  public void testFileOverviewSingleLine() throws Exception {
    JSDocInfo jsdoc = parseFileOverview("@fileoverview Hi mom! */");
    assertEquals("Hi mom!", jsdoc.getFileOverview());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewMultiLine
  public void testFileOverviewMultiLine() throws Exception {
    JSDocInfo jsdoc = parseFileOverview("@fileoverview Pie is \n * good! */");
    assertEquals("Pie is\n good!", jsdoc.getFileOverview());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewDuplicate
  public void testFileOverviewDuplicate() throws Exception {
    parseFileOverview(
        "@fileoverview Pie \n * @fileoverview Cake */",
        "extra @fileoverview tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testReferences
  public void testReferences() throws Exception {
    JSDocInfo jsdoc
        = parse("@see A cool place!"
                + "\n* @see The world."
                + "\n* @see SomeClass#SomeMember"
                + "\n* @see A boring test case*/"
                , true);

    Collection<String> references = jsdoc.getReferences();

    assertTrue(references != null);
    assertTrue(references.size() == 4);

    assertContains(references, "A cool place!");
    assertContains(references, "The world.");
    assertContains(references, "SomeClass#SomeMember");
    assertContains(references, "A boring test case");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSingleTags
  public void testSingleTags() throws Exception {
    JSDocInfo jsdoc
        = parse("@version Some old version"
                + "\n* @deprecated In favor of the new one!"
                + "\n* @return {SomeType} The most important object :-)*/"
                , true);

    assertTrue(jsdoc.isDeprecated());
    assertEquals("In favor of the new one!", jsdoc.getDeprecationReason());
    assertEquals("Some old version", jsdoc.getVersion());
    assertEquals("The most important object :-)", jsdoc.getReturnDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSingleTags2
  public void testSingleTags2() throws Exception {
    JSDocInfo jsdoc = parse(
        "@param {SomeType} a The most important object :-)*/", true);

    assertEquals("The most important object :-)",
        jsdoc.getDescriptionForParameter("a"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSingleTagsReordered
  public void testSingleTagsReordered() throws Exception {
    JSDocInfo jsdoc
        = parse("@deprecated In favor of the new one!"
                + "\n * @return {SomeType} The most important object :-)"
                + "\n * @version Some old version*/"
                , true);

    assertTrue(jsdoc.isDeprecated());
    assertEquals("In favor of the new one!", jsdoc.getDeprecationReason());
    assertEquals("Some old version", jsdoc.getVersion());
    assertEquals("The most important object :-)", jsdoc.getReturnDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testVersionDuplication
  public void testVersionDuplication() throws Exception {
    parse("* @version Some old version"
          + "\n* @version Another version*/", true,
          "conflicting @version tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testVersionMissing
  public void testVersionMissing() throws Exception {
    parse("* @version */", true,
          "@version tag missing version information");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testAuthorMissing
  public void testAuthorMissing() throws Exception {
    parse("* @author */", true,
          "@author tag missing author");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSeeMissing
  public void testSeeMissing() throws Exception {
    parse("* @see */", true,
          "@see tag missing description");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSourceName
  public void testSourceName() throws Exception {
    JSDocInfo jsdoc = parse("@deprecated */", true);
    assertEquals("testcode", jsdoc.getAssociatedNode().getSourceFileName());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment
  public void testParseBlockComment() throws Exception {
    JSDocInfo jsdoc = parse("this is a nice comment\n "
                            + "* that is multiline \n"
                            + "* @author abc@google.com */", true);

    assertEquals("this is a nice comment\nthat is multiline",
                 jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment2
  public void testParseBlockComment2() throws Exception {
    JSDocInfo jsdoc = parse("this is a nice comment\n "
                            + "* that is *** multiline \n"
                            + "* @author abc@google.com */", true);

    assertEquals("this is a nice comment\nthat is *** multiline",
                 jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment3
  public void testParseBlockComment3() throws Exception {
    JSDocInfo jsdoc = parse("\n "
                            + "* hello world \n"
                            + "* @author abc@google.com */", true);

    assertEquals("hello world", jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers1
  public void testParseWithMarkers1() throws Exception {
    JSDocInfo jsdoc = parse("@author abc@google.com */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 0, 0),
        "abc@google.com", 7, 0, 21);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers2
  public void testParseWithMarkers2() throws Exception {
    JSDocInfo jsdoc = parse("@param {Foo} somename abc@google.com */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "param", 0, 0),
        "abc@google.com", 21, 0, 37);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers3
  public void testParseWithMarkers3() throws Exception {
    JSDocInfo jsdoc =
        parse("@return {Foo} some long \n * multiline" +
              " \n * description */", true);

    JSDocInfo.Marker returnDoc =
        assertAnnotationMarker(jsdoc, "return", 0, 0);
    assertDocumentationInMarker(returnDoc,
        "some long multiline description", 14, 2, 15);
    assertEquals(8, returnDoc.getType().getPositionOnStartLine());
    assertEquals(12, returnDoc.getType().getPositionOnEndLine());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers4
  public void testParseWithMarkers4() throws Exception {
    JSDocInfo jsdoc =
        parse("@author foobar \n * @param {Foo} somename abc@google.com */",
              true);

    assertAnnotationMarker(jsdoc, "author", 0, 0);
    assertAnnotationMarker(jsdoc, "param", 1, 3);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers5
  public void testParseWithMarkers5() throws Exception {
    JSDocInfo jsdoc =
        parse("@return some long \n * multiline" +
              " \n * description */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "return", 0, 0),
        "some long multiline description", 8, 2, 15);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers6
  public void testParseWithMarkers6() throws Exception {
    JSDocInfo jsdoc =
        parse("@param x some long \n * multiline" +
              " \n * description */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "param", 0, 0),
        "some long multiline description", 8, 2, 15);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerNames1
  public void testParseWithMarkerNames1() throws Exception {
    JSDocInfo jsdoc = parse("@param {SomeType} name somedescription */", true);

    assertNameInMarker(
        assertAnnotationMarker(jsdoc, "param", 0, 0),
        "name", 0, 18);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerNames2
  public void testParseWithMarkerNames2() throws Exception {
    JSDocInfo jsdoc = parse("@param {SomeType} name somedescription \n" +
                            "* @param {AnotherType} anothername des */", true);

    assertTypeInMarker(
        assertNameInMarker(
            assertAnnotationMarker(jsdoc, "param", 0, 0, 0),
            "name", 0, 18),
        "SomeType", 0, 7, 0, 16, true);

    assertTypeInMarker(
        assertNameInMarker(
            assertAnnotationMarker(jsdoc, "param", 1, 2, 1),
            "anothername", 1, 23),
        "AnotherType", 1, 9, 1, 21, true);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerNames3
  public void testParseWithMarkerNames3() throws Exception {
    JSDocInfo jsdoc = parse(
        "@param {Some.Long.Type.\n *  Name} name somedescription */", true);

    assertTypeInMarker(
        assertNameInMarker(
            assertAnnotationMarker(jsdoc, "param", 0, 0, 0),
            "name", 1, 10),
        "Some.Long.Type.Name", 0, 7, 1, 8, true);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithoutMarkerName
  public void testParseWithoutMarkerName() throws Exception {
    JSDocInfo jsdoc = parse("@author helloworld*/", true);
    assertNull(assertAnnotationMarker(jsdoc, "author", 0, 0).getName());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerType
  public void testParseWithMarkerType() throws Exception {
    JSDocInfo jsdoc = parse("@extends {FooBar}*/", true);

    assertTypeInMarker(
        assertAnnotationMarker(jsdoc, "extends", 0, 0),
        "FooBar", 0, 9, 0, 16, true);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerType2
  public void testParseWithMarkerType2() throws Exception {
    JSDocInfo jsdoc = parse("@extends FooBar*/", true);

    assertTypeInMarker(
        assertAnnotationMarker(jsdoc, "extends", 0, 0),
        "FooBar", 0, 9, 0, 15, false);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict1
  public void testTypeTagConflict1() throws Exception {
    parse("@constructor \n * @constructor */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict2
  public void testTypeTagConflict2() throws Exception {
    parse("@interface \n * @interface */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict3
  public void testTypeTagConflict3() throws Exception {
    parse("@constructor \n * @interface */",
        "Bad type annotation. cannot be both an interface and a constructor");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict4
  public void testTypeTagConflict4() throws Exception {
    parse("@interface \n * @constructor */",
        "Bad type annotation. cannot be both an interface and a constructor");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict5
  public void testTypeTagConflict5() throws Exception {
    parse("@interface \n * @type {string} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict6
  public void testTypeTagConflict6() throws Exception {
    parse("@typedef {string} \n * @type {string} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict7
  public void testTypeTagConflict7() throws Exception {
    parse("@typedef {string} \n * @constructor */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict8
  public void testTypeTagConflict8() throws Exception {
    parse("@typedef {string} \n * @return {boolean} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict9
  public void testTypeTagConflict9() throws Exception {
    parse("@enum {string} \n * @return {boolean} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict10
  public void testTypeTagConflict10() throws Exception {
    parse("@this {Object} \n * @enum {boolean} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict11
  public void testTypeTagConflict11() throws Exception {
    parse("@param {Object} x \n * @type {boolean} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict12
  public void testTypeTagConflict12() throws Exception {
    parse("@typedef {boolean} \n * @param {Object} x */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict13
  public void testTypeTagConflict13() throws Exception {
    parse("@typedef {boolean} \n * @extends {Object} */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict14
  public void testTypeTagConflict14() throws Exception {
    parse("@return x \n * @return y */",
        "Bad type annotation. " +
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict15
  public void testTypeTagConflict15() throws Exception {
    parse("\n" +
          "function StrStr() {}",
          "Bad type annotation. " +
          "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict16
  public void testTypeTagConflict16() throws Exception {
    parse("\n" +
          "function StrIntf() {}",
          "Bad type annotation. " +
          "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict17
  public void testTypeTagConflict17() throws Exception {
    parse("\n" +
          "function StrIntf() {}",
          "Bad type annotation. " +
          "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict18
  public void testTypeTagConflict18() throws Exception {
    parse("\n" +
          "function DictDict() {}",
          "Bad type annotation. " +
          "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict19
  public void testTypeTagConflict19() throws Exception {
    parse("\n" +
          "function DictDict() {}",
          "Bad type annotation. " +
          "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict20
  public void testTypeTagConflict20() throws Exception {
    parse("\n" +
          "function DictDict() {}",
          "Bad type annotation. " +
          "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict21
  public void testTypeTagConflict21() throws Exception {
    parse("\n" +
          "function DictDict() {}",
          "Bad type annotation. " +
          "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict22
  public void testTypeTagConflict22() throws Exception {
    parse("\n" +
          "function DictDict(x) {}",
          "Bad type annotation. " +
          "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict23
  public void testTypeTagConflict23() throws Exception {
    parse("\n" +
          "function DictDict() {}",
          "Bad type annotation. " +
          "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict24
  public void testTypeTagConflict24() throws Exception {
    parse("\n" +
          "function DictDict() {}",
          "Bad type annotation. " +
          "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testPrivateType
  public void testPrivateType() throws Exception {
    JSDocInfo jsdoc = parse("@private {string} */");
    assertTypeEquals(STRING_TYPE, jsdoc.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testProtectedType
  public void testProtectedType() throws Exception {
    JSDocInfo jsdoc = parse("@protected {string} */");
    assertTypeEquals(STRING_TYPE, jsdoc.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testPublicType
  public void testPublicType() throws Exception {
    JSDocInfo jsdoc = parse("@public {string} */");
    assertTypeEquals(STRING_TYPE, jsdoc.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testConstType
  public void testConstType() throws Exception {
    JSDocInfo jsdoc = parse("@const {string} */");
    assertTypeEquals(STRING_TYPE, jsdoc.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testStableIdGeneratorConflict
  public void testStableIdGeneratorConflict() throws Exception {
    parse("\n" +
          "function getId() {}",
          "extra @stableIdGenerator tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIdGenerator
  public void testIdGenerator() throws Exception {
    JSDocInfo info = parse("\n" +
          "function getId() {}");
    assertTrue(info.isIdGenerator());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIdGeneratorConflict
  public void testIdGeneratorConflict() throws Exception {
    parse("\n" +
          "function getId() {}",
          "extra @idGenerator tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIdGenerator1
  public void testIdGenerator1() throws Exception {
    JSDocInfo info = parse("@idGenerator {unique} */");
    assertTrue(info.isIdGenerator());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIdGenerator2
  public void testIdGenerator2() throws Exception {
    JSDocInfo info = parse("@idGenerator {consistent} */");
    assertTrue(info.isConsistentIdGenerator());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIdGenerator3
  public void testIdGenerator3() throws Exception {
    JSDocInfo info = parse("@idGenerator {stable} */");
    assertTrue(info.isStableIdGenerator());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIdGenerator4
  public void testIdGenerator4() throws Exception {
    JSDocInfo info = parse("@idGenerator {mapped} */");
    assertTrue(info.isMappedIdGenerator());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadIdGenerator1
  public void testBadIdGenerator1() throws Exception {
    parse("@idGenerator {} */", "malformed @idGenerator tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadIdGenerator2
  public void testBadIdGenerator2() throws Exception {
    parse("@idGenerator {impossible} */",
        "unknown @idGenerator parameter: impossible");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadIdGenerator3
  public void testBadIdGenerator3() throws Exception {
    parse("@idGenerator {unique */", "malformed @idGenerator tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParserWithTemplateTypeNameMissing
  public void testParserWithTemplateTypeNameMissing() {
    parse("@template */",
        "Bad type annotation. @template tag missing type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParserWithTemplateDuplicated
  public void testParserWithTemplateDuplicated() {
    parse("@template T\n@template V */",
        "Bad type annotation. @template tag at most once");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParserWithTwoTemplates
  public void testParserWithTwoTemplates() {
    parse("@template T,V */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedNewAnnotations
  public void testWhitelistedNewAnnotations() {
    parse("@foobar */",
        "illegal use of unknown JSDoc tag \"foobar\"; ignoring it");
    extraAnnotations.add("foobar");
    parse("@foobar */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedConflictingAnnotation
  public void testWhitelistedConflictingAnnotation() {
    extraAnnotations.add("param");
    JSDocInfo info = parse("@param {number} index */");
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testNonIdentifierAnnotation
  public void testNonIdentifierAnnotation() {
    
    
    extraAnnotations.add("123");
    parse("@123 */", "illegal use of unknown JSDoc tag \"\"; ignoring it");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testUnsupportedJsDocSyntax1
  public void testUnsupportedJsDocSyntax1() {
    JSDocInfo info =
        parse("@param {string} [accessLevel=\"author\"] The user level */",
            true);
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(STRING_TYPE),
        info.getParameterType("accessLevel"));
    assertEquals("The user level",
        info.getDescriptionForParameter("accessLevel"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testUnsupportedJsDocSyntax2
  public void testUnsupportedJsDocSyntax2() {
    JSDocInfo info =
        parse("@param userInfo The user info. \n" +
              " * @param userInfo.name The name of the user */", true);
    assertEquals(1, info.getParameterCount());
    assertEquals("The user info.",
        info.getDescriptionForParameter("userInfo"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedAnnotations
  public void testWhitelistedAnnotations() {
    parse(
      "* @addon \n" +
      "* @augments \n" +
      "* @base \n" +
      "* @borrows \n" +
      "* @bug \n" +
      "* @class \n" +
      "* @config \n" +
      "* @constructs \n" +
      "* @default \n" +
      "* @description \n" +
      "* @event \n" +
      "* @example \n" +
      "* @exception \n" +
      "* @exec \n" +
      "* @externs \n" +
      "* @field \n" +
      "* @function \n" +
      "* @hassoydelcall \n" +
      "* @hassoydeltemplate \n" +
      "* @id \n" +
      "* @ignore \n" +
      "* @inner \n" +
      "* @jaggerInject \n" +
      "* @jaggerModule \n" +
      "* @jaggerProvide \n" +
      "* @lends {string} \n" +
      "* @link \n" +
      "* @member \n" +
      "* @memberOf \n" +
      "* @modName \n" +
      "* @mods \n" +
      "* @name \n" +
      "* @namespace \n" +
      "* @ngInject \n" +
      "* @nocompile \n" +
      "* @property \n" +
      "* @requirecss \n" +
      "* @requires \n" +
      "* @since \n" +
      "* @static \n" +
      "* @supported\n" +
      "* @wizaction \n" +
      "* @wizmodule \n" +
      "*/");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testJsDocInfoPosition
  public void testJsDocInfoPosition() throws IOException {
    SourceFile sourceFile = SourceFile.fromCode("comment-position-test.js",
        "   \n" +
        "  \n" +
        "  function double(x) {}");
    List<JSDocInfo> jsdocs = parseFull(sourceFile.getCode());
    assertEquals(1, jsdocs.size());
    assertEquals(6, jsdocs.get(0).getOriginalCommentPosition());
    assertEquals(2, sourceFile.getLineOfOffset(jsdocs.get(0).getOriginalCommentPosition()));
    assertEquals(2, sourceFile.getColumnOfOffset(jsdocs.get(0).getOriginalCommentPosition()));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testGetOriginalCommentString
  public void testGetOriginalCommentString() throws Exception {
    String comment = "* @desc This is a comment */";
    JSDocInfo info = parse(comment);
    assertNull(info.getOriginalCommentString());
    info = parse(comment, true );
    assertEquals(comment, info.getOriginalCommentString());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNgInject1
  public void testParseNgInject1() throws Exception {
    assertTrue(parse("@ngInject*/").isNgInject());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNgInject2
  public void testParseNgInject2() throws Exception {
    parse("@ngInject \n@ngInject*/", "extra @ngInject tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJaggerInject
  public void testParseJaggerInject() throws Exception {
    assertTrue(parse("@jaggerInject*/").isJaggerInject());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJaggerInjectExtra
  public void testParseJaggerInjectExtra() throws Exception {
    parse("@jaggerInject \n@jaggerInject*/", "extra @jaggerInject tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJaggerModule
  public void testParseJaggerModule() throws Exception {
    assertTrue(parse("@jaggerModule*/").isJaggerModule());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJaggerModuleExtra
  public void testParseJaggerModuleExtra() throws Exception {
    parse("@jaggerModule \n@jaggerModule*/", "extra @jaggerModule tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJaggerProvide
  public void testParseJaggerProvide() throws Exception {
    assertTrue(parse("@jaggerProvide*/").isJaggerProvide());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJaggerProvideExtra
  public void testParseJaggerProvideExtra() throws Exception {
    parse("@jaggerProvide \n@jaggerProvide*/", "extra @jaggerProvide tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWizaction1
  public void testParseWizaction1() throws Exception {
    assertTrue(parse("@wizaction*/").isWizaction());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWizaction2
  public void testParseWizaction2() throws Exception {
    parse("@wizaction \n@wizaction*/", "extra @wizaction tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDisposes1
  public void testParseDisposes1() throws Exception {
    assertTrue(parse("@param x \n * @disposes x */").isDisposes());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDisposes2
  public void testParseDisposes2() throws Exception {
    parse("@param x \n * @disposes */",
        true, "Bad type annotation. @disposes tag missing parameter name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDisposes3
  public void testParseDisposes3() throws Exception {
    assertTrue(parse("@param x \n @param y\n * @disposes x, y */").isDisposes());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDisposesUnknown
  public void testParseDisposesUnknown() throws Exception {
    parse("@param x \n * @disposes x,y */",
        true,
        "Bad type annotation. @disposes parameter unknown or parameter specified multiple times");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDisposesMultiple
  public void testParseDisposesMultiple() throws Exception {
    parse("@param x \n * @disposes x,x */",
        true,
        "Bad type annotation. @disposes parameter unknown or parameter specified multiple times");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDisposesAll1
  public void testParseDisposesAll1() throws Exception {
    assertTrue(parse("@param x \n * @disposes * */").isDisposes());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDisposesAll2
  public void testParseDisposesAll2() throws Exception {
    assertTrue(parse("@param x \n * @disposes x,* */").isDisposes());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDisposesAll3
  public void testParseDisposesAll3() throws Exception {
    parse("@param x \n * @disposes *, * */",
        true,
        "Bad type annotation. @disposes parameter unknown or parameter specified multiple times");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTextExtents
  public void testTextExtents() {
    parse("@return {@code foo} bar \n *    baz. */",
        true, "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAssign1
  public void testLinenoCharnoAssign1() throws Exception {
    Node assign = parse("a = b").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(1, assign.getLineno());
    assertEquals(0, assign.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAssign2
  public void testLinenoCharnoAssign2() throws Exception {
    Node assign = parse("\n a.g.h.k    =  45").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(2, assign.getLineno());
    assertEquals(1, assign.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoCall
  public void testLinenoCharnoCall() throws Exception {
    Node call = parse("\n foo(123);").getFirstChild().getFirstChild();

    assertEquals(Token.CALL, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetProp1
  public void testLinenoCharnoGetProp1() throws Exception {
    Node getprop = parse("\n foo.bar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(2, getprop.getLineno());
    assertEquals(1, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(2, name.getLineno());
    assertEquals(5, name.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetProp2
  public void testLinenoCharnoGetProp2() throws Exception {
    Node getprop = parse("\n foo.\nbar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(2, getprop.getLineno());
    assertEquals(1, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(3, name.getLineno());
    assertEquals(0, name.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem1
  public void testLinenoCharnoGetelem1() throws Exception {
    Node call = parse("\n foo[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem2
  public void testLinenoCharnoGetelem2() throws Exception {
    Node call = parse("\n   \n foo()[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(3, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem3
  public void testLinenoCharnoGetelem3() throws Exception {
    Node call = parse("\n   \n (8 + kl)[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(3, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoForComparison
  public void testLinenoCharnoForComparison() throws Exception {
    Node lt =
      parse("for (; i < j;){}").getFirstChild().getFirstChild().getNext();

    assertEquals(Token.LT, lt.getType());
    assertEquals(1, lt.getLineno());
    assertEquals(7, lt.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoHook
  public void testLinenoCharnoHook() throws Exception {
    Node n = parse("\n a ? 9 : 0").getFirstChild().getFirstChild();

    assertEquals(Token.HOOK, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(1, n.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoArrayLiteral
  public void testLinenoCharnoArrayLiteral() throws Exception {
    Node n = parse("\n  [8, 9]").getFirstChild().getFirstChild();

    assertEquals(Token.ARRAYLIT, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(2, n.getCharno());

    n = n.getFirstChild();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(3, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(6, n.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoObjectLiteral
  public void testLinenoCharnoObjectLiteral() throws Exception {
    Node n = parse("\n\n var a = {a:0\n,b :1};")
        .getFirstChild().getFirstChild().getFirstChild();

    assertEquals(Token.OBJECTLIT, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(9, n.getCharno());

    Node key = n.getFirstChild();

    assertEquals(Token.STRING_KEY, key.getType());
    assertEquals(3, key.getLineno());
    assertEquals(10, key.getCharno());

    Node value = key.getFirstChild();

    assertEquals(Token.NUMBER, value.getType());
    assertEquals(3, value.getLineno());
    assertEquals(12, value.getCharno());

    key = key.getNext();

    assertEquals(Token.STRING_KEY, key.getType());
    assertEquals(4, key.getLineno());
    assertEquals(1, key.getCharno());

    value = key.getFirstChild();

    assertEquals(Token.NUMBER, value.getType());
    assertEquals(4, value.getLineno());
    assertEquals(4, value.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAdd
  public void testLinenoCharnoAdd() throws Exception {
    testLinenoCharnoBinop("+");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoSub
  public void testLinenoCharnoSub() throws Exception {
    testLinenoCharnoBinop("-");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoMul
  public void testLinenoCharnoMul() throws Exception {
    testLinenoCharnoBinop("*");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoDiv
  public void testLinenoCharnoDiv() throws Exception {
    testLinenoCharnoBinop("/");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoMod
  public void testLinenoCharnoMod() throws Exception {
    testLinenoCharnoBinop("%");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoShift
  public void testLinenoCharnoShift() throws Exception {
    testLinenoCharnoBinop("<<");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoBinaryAnd
  public void testLinenoCharnoBinaryAnd() throws Exception {
    testLinenoCharnoBinop("&");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAnd
  public void testLinenoCharnoAnd() throws Exception {
    testLinenoCharnoBinop("&&");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoBinaryOr
  public void testLinenoCharnoBinaryOr() throws Exception {
    testLinenoCharnoBinop("|");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoOr
  public void testLinenoCharnoOr() throws Exception {
    testLinenoCharnoBinop("||");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoLt
  public void testLinenoCharnoLt() throws Exception {
    testLinenoCharnoBinop("<");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoLe
  public void testLinenoCharnoLe() throws Exception {
    testLinenoCharnoBinop("<=");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGt
  public void testLinenoCharnoGt() throws Exception {
    testLinenoCharnoBinop(">");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGe
  public void testLinenoCharnoGe() throws Exception {
    testLinenoCharnoBinop(">=");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment1
  public void testJSDocAttachment1() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment2
  public void testJSDocAttachment2() {
    Node varNode = parse("var a,b;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node nameNode1 = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode1.getType());
    assertNull(nameNode1.getJSDocInfo());

    
    Node nameNode2 = nameNode1.getNext();
    assertEquals(Token.NAME, nameNode2.getType());
    assertNull(nameNode2.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment3
  public void testJSDocAttachment3() {
    Node assignNode = parse(
        "goog.FOO = 5;").getFirstChild().getFirstChild();
    assertEquals(Token.ASSIGN, assignNode.getType());
    JSDocInfo info = assignNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment4
  public void testJSDocAttachment4() {
    Node varNode = parse(
        "var a,  b = 5;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    assertNull(varNode.getJSDocInfo());

    
    Node a = varNode.getFirstChild();
    assertNull(a.getJSDocInfo());

    
    Node b = a.getNext();
    JSDocInfo info = b.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment5
  public void testJSDocAttachment5() {
    Node varNode = parse(
        "var a, b = 5;")
        .getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    assertNull(varNode.getJSDocInfo());

    
    Node a = varNode.getFirstChild();
    assertNotNull(a.getJSDocInfo());
    JSDocInfo info = a.getJSDocInfo();
    assertNotNull(info);
    assertFalse(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node b = a.getNext();
    info = b.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment6
  public void testJSDocAttachment6() throws Exception {
    Node functionNode = parse(
        "var a = 5;" +
        "function f(index){}")
        .getFirstChild().getNext();

    assertEquals(Token.FUNCTION, functionNode.getType());
    JSDocInfo info = functionNode.getJSDocInfo();
    assertNotNull(info);
    assertFalse(info.hasParameter("index"));
    assertTrue(info.hasReturnType());
    assertTypeEquals(UNKNOWN_TYPE, info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment7
  public void testJSDocAttachment7() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment8
  public void testJSDocAttachment8() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment9
  public void testJSDocAttachment9() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment10
  public void testJSDocAttachment10() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment11
  public void testJSDocAttachment11() {
    Node varNode =
       parse("var a;")
        .getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);

    assertTypeEquals(createRecordTypeBuilder().
                     addProperty("x", NUMBER_TYPE, null).
                     addProperty("y", STRING_TYPE, null).
                     addProperty("z", UNKNOWN_TYPE, null).
                     build(),
                     info.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment12
  public void testJSDocAttachment12() {
    Node varNode =
       parse("var a = { b: c};")
        .getFirstChild();
    Node objectLitNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLitNode.getType());
    assertNotNull(objectLitNode.getFirstChild().getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment13
  public void testJSDocAttachment13() {
    Node varNode = parse(" var a;").getFirstChild();
    assertNotNull(varNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment14
  public void testJSDocAttachment14() {
    Node varNode = parse(" var a;").getFirstChild();
    assertNull(varNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment15
  public void testJSDocAttachment15() {
    Node varNode = parse(" var a;").getFirstChild();
    assertNull(varNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment16
  public void testJSDocAttachment16() {
    Node exprCall =
        parse(" x(); function f() {};").getFirstChild();
    assertEquals(Token.EXPR_RESULT, exprCall.getType());
    assertNull(exprCall.getNext().getJSDocInfo());
    assertNotNull(exprCall.getFirstChild().getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment17
  public void testJSDocAttachment17() {
    Node fn =
        parse(
            "function f() { " +
            "  return  (g(1 ));" +
            "};").getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    Node cast = fn.getLastChild().getFirstChild().getFirstChild();
    assertEquals(Token.CAST, cast.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment18
  public void testJSDocAttachment18() {
    Node fn =
        parse(
            "function f() { " +
            "  var x =  (y);" +
            "};").getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    Node cast =
        fn.getLastChild().getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.CAST, cast.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment19
  public void testJSDocAttachment19() {
    Node fn =
        parse(
            "function f() { " +
            "  " +
            "  return;" +
            "};").getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());

    Node ret = fn.getLastChild().getFirstChild();
    assertEquals(Token.RETURN, ret.getType());
    assertNull(ret.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment20
  public void testJSDocAttachment20() {
    Node fn =
        parse(
            "function f() { " +
            "  " +
            "  if (true) return;" +
            "};").getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());

    Node ret = fn.getLastChild().getFirstChild();
    assertEquals(Token.IF, ret.getType());
    assertNull(ret.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testInlineJSDocAttachment1
  public void testInlineJSDocAttachment1() {
    Node fn = parse("function f( x) {}").getFirstChild();
    assertTrue(fn.isFunction());

    JSDocInfo info =
        fn.getFirstChild().getNext().getFirstChild().getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testInlineJSDocAttachment2
  public void testInlineJSDocAttachment2() {
    Node fn = parse(
        "function f( x) {}").getFirstChild();
    assertTrue(fn.isFunction());

    JSDocInfo info =
        fn.getFirstChild().getNext().getFirstChild().getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(UNKNOWN_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testInlineJSDocAttachment3
  public void testInlineJSDocAttachment3() {
    parse(
        "function f( x) {}",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testInlineJSDocAttachment4
  public void testInlineJSDocAttachment4() {
    parse(
        "function f( x) {}",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testInlineJSDocAttachment5
  public void testInlineJSDocAttachment5() {
    Node vardecl = parse("var  x = 'asdf';").getFirstChild();
    JSDocInfo info = vardecl.getFirstChild().getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testInlineJSDocAttachment6
  public void testInlineJSDocAttachment6() {
    Node fn = parse("function f( x) {}").getFirstChild();
    assertTrue(fn.isFunction());

    JSDocInfo info =
        fn.getFirstChild().getNext().getFirstChild().getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(createRecordTypeBuilder().
        addProperty("attr", NUMBER_TYPE, null).
        build(),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing1
  public void testIncorrectJSDocDoesNotAlterJSParsing1() throws Exception {
    assertNodeEquality(
        parse("var a = [1,2]"),
        parse("var a = [1,2]",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing2
  public void testIncorrectJSDocDoesNotAlterJSParsing2() throws Exception {
    assertNodeEquality(
        parse("var a = [1,2]"),
        parse("var a = [1,2]",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing3
  public void testIncorrectJSDocDoesNotAlterJSParsing3() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing4
  public void testIncorrectJSDocDoesNotAlterJSParsing4() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing5
  public void testIncorrectJSDocDoesNotAlterJSParsing5() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing6
  public void testIncorrectJSDocDoesNotAlterJSParsing6() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
            "Bad type annotation. expected closing }",
            "Bad type annotation. expecting a variable name in a @param tag"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing7
  public void testIncorrectJSDocDoesNotAlterJSParsing7() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "@see tag missing description"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing8
  public void testIncorrectJSDocDoesNotAlterJSParsing8() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "@author tag missing author"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing9
  public void testIncorrectJSDocDoesNotAlterJSParsing9() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
              "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "illegal use of unknown JSDoc tag \"someillegaltag\";"
              + " ignoring it"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testUnescapedSlashInRegexpCharClass
  public void testUnescapedSlashInRegexpCharClass() throws Exception {
    
    parse("var foo = /[/]/;");
    parse("var foo = /[hi there/]/;");
    parse("var foo = /[/yo dude]/;");
    parse("var foo = /\\/[@#$/watashi/wa/suteevu/desu]/;");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testParse
  public void testParse() {
    Node a = Node.newString(Token.NAME, "a");
    a.addChildToFront(Node.newString(Token.NAME, "b"));
    List<ParserResult> testCases = ImmutableList.of(
        new ParserResult(
            "3;",
            createScript(new Node(Token.EXPR_RESULT, Node.newNumber(3.0)))),
        new ParserResult(
            "var a = b;",
             createScript(new Node(Token.VAR, a))),
        new ParserResult(
            "\"hell\\\no\\ world\\\n\\\n!\"",
             createScript(new Node(Token.EXPR_RESULT,
             Node.newString(Token.STRING, "hello world!")))));

    for (ParserResult testCase : testCases) {
      assertNodeEquality(testCase.node, parse(testCase.code));
    }
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning1
  public void testTrailingCommaWarning1() {
    parse("var a = ['foo', 'bar'];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning2
  public void testTrailingCommaWarning2() {
    parse("var a = ['foo',,'bar'];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning3
  public void testTrailingCommaWarning3() {
    parse("var a = ['foo', 'bar',];", TRAILING_COMMA_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var a = ['foo', 'bar',];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning4
  public void testTrailingCommaWarning4() {
    parse("var a = [,];", TRAILING_COMMA_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var a = [,];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning5
  public void testTrailingCommaWarning5() {
    parse("var a = {'foo': 'bar'};");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning6
  public void testTrailingCommaWarning6() {
    parse("var a = {'foo': 'bar',};", TRAILING_COMMA_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var a = {'foo': 'bar',};");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning7
  public void testTrailingCommaWarning7() {
    parseError("var a = {,};", BAD_PROPERTY_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSuspiciousBlockCommentWarning1
  public void testSuspiciousBlockCommentWarning1() {
    parse(" var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSuspiciousBlockCommentWarning2
  public void testSuspiciousBlockCommentWarning2() {
    parse(" var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSuspiciousBlockCommentWarning3
  public void testSuspiciousBlockCommentWarning3() {
    parse(" var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSuspiciousBlockCommentWarning4
  public void testSuspiciousBlockCommentWarning4() {
    parse(
        "  \n" +
        "  var x = 3;",
        SUSPICIOUS_COMMENT_WARNING);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSuspiciousBlockCommentWarning5
  public void testSuspiciousBlockCommentWarning5() {
    parse(
        "  \n" +
        "  var x = 3;",
        SUSPICIOUS_COMMENT_WARNING);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSuspiciousBlockCommentWarning6
  public void testSuspiciousBlockCommentWarning6() {
    parse(" var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSuspiciousBlockCommentWarning7
  public void testSuspiciousBlockCommentWarning7() {
    
    parse(" var x = 3;");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSuspiciousBlockCommentWarning8
  public void testSuspiciousBlockCommentWarning8() {
    
    parse(" var x = 3;");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testCatchClauseForbidden
  public void testCatchClauseForbidden() {
    parseError("try { } catch (e if true) {}",
        "Catch clauses are not supported");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testConstForbidden
  public void testConstForbidden() {
    parseError("const x = 3;", "Unsupported syntax: CONST");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden
  public void testDestructuringAssignForbidden() {
    parseError("var [x, y] = foo();", "destructuring assignment forbidden");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden2
  public void testDestructuringAssignForbidden2() {
    parseError("var {x, y} = foo();", "missing : after property id");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden3
  public void testDestructuringAssignForbidden3() {
    parseError("var {x: x, y: y} = foo();",
        "destructuring assignment forbidden");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden4
  public void testDestructuringAssignForbidden4() {
    parseError("[x, y] = foo();",
        "destructuring assignment forbidden",
        "invalid assignment target");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLetForbidden
  public void testLetForbidden() {
    parseError("function f() { let (x = 3) { alert(x); }; }",
        "missing ; before statement", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testYieldForbidden
  public void testYieldForbidden() {
    parseError("function f() { yield 3; }", "missing ; before statement");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testBracelessFunctionForbidden
  public void testBracelessFunctionForbidden() {
    parseError("var sq = function(x) x * x;",
        "missing { before function body");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGeneratorsForbidden
  public void testGeneratorsForbidden() {
    parseError("var i = (x for (x in obj));",
        "Unsupported syntax: GENEXPR");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden1
  public void testGettersForbidden1() {
    parseError("var x = {get foo() { return 3; }};",
        IRFactory.GETTER_ERROR_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden2
  public void testGettersForbidden2() {
    parseError("var x = {get foo bar() { return 3; }};",
        "invalid property id");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden3
  public void testGettersForbidden3() {
    parseError("var x = {a getter:function b() { return 3; }};",
        "missing : after property id", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden4
  public void testGettersForbidden4() {
    parseError("var x = {\"a\" getter:function b() { return 3; }};",
        "missing : after property id", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGettersForbidden5
  public void testGettersForbidden5() {
    parseError("var x = {a: 2, get foo() { return 3; }};",
        IRFactory.GETTER_ERROR_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSettersForbidden
  public void testSettersForbidden() {
    parseError("var x = {set foo() { return 3; }};",
        IRFactory.SETTER_ERROR_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSettersForbidden2
  public void testSettersForbidden2() {
    parseError("var x = {a setter:function b() { return 3; }};",
        "missing : after property id", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDoc1
  public void testFileOverviewJSDoc1() {
    Node n = parse(" function Foo() {}");
    assertEquals(Token.FUNCTION, n.getFirstChild().getType());
    assertTrue(n.getJSDocInfo() != null);
    assertNull(n.getFirstChild().getJSDocInfo());
    assertEquals("Hi mom!",
        n.getJSDocInfo().getFileOverview());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDocDoesNotHoseParsing
  public void testFileOverviewJSDocDoesNotHoseParsing() {
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDoc2
  public void testFileOverviewJSDoc2() {
    Node n = parse(" " +
        " function Foo() {}");
    assertTrue(n.getJSDocInfo() != null);
    assertEquals("Hi mom!", n.getJSDocInfo().getFileOverview());
    assertTrue(n.getFirstChild().getJSDocInfo() != null);
    assertFalse(n.getFirstChild().getJSDocInfo().hasFileOverview());
    assertTrue(n.getFirstChild().getJSDocInfo().isConstructor());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testObjectLiteralDoc1
  public void testObjectLiteralDoc1() {
    Node n = parse("var x = { 1: 2};");

    Node objectLit = n.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLit.getType());

    Node number = objectLit.getFirstChild();
    assertEquals(Token.STRING_KEY, number.getType());
    assertNotNull(number.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDuplicatedParam
  public void testDuplicatedParam() {
    parse("function foo(x, x) {}", "Duplicate parameter name \"x\".");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGetter
  public void testGetter() {
    mode = LanguageMode.ECMASCRIPT3;
    parseError("var x = {get 1(){}};",
        IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {get 'a'(){}};",
        IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {get a(){}};",
        IRFactory.GETTER_ERROR_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var x = {get 1(){}};");
    parse("var x = {get 'a'(){}};");
    parse("var x = {get a(){}};");
    parseError("var x = {get a(b){}};", "getters may not have parameters");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testSetter
  public void testSetter() {
    mode = LanguageMode.ECMASCRIPT3;
    parseError("var x = {set 1(x){}};",
        IRFactory.SETTER_ERROR_MESSAGE);
    parseError("var x = {set 'a'(x){}};",
        IRFactory.SETTER_ERROR_MESSAGE);
    parseError("var x = {set a(x){}};",
        IRFactory.SETTER_ERROR_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var x = {set 1(x){}};");
    parse("var x = {set 'a'(x){}};");
    parse("var x = {set a(x){}};");
    parseError("var x = {set a(){}};",
        "setters must have exactly one parameter");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLamestWarningEver
  public void testLamestWarningEver() {
    
    parse("var x =  (y);");
    parse("var x =  (y);");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testUnfinishedComment
  public void testUnfinishedComment() {
    parseError(" var x;");
    Node var = n.getFirstChild();
    assertNotNull(var.getJSDocInfo());
    assertEquals("This is a variable.",
        var.getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testUnnamedFunctionStatement
  public void testUnnamedFunctionStatement() {
    
    parseError("function() {};", "unnamed function statement");
    parseError("if (true) { function() {}; }", "unnamed function statement");
    parse("function f() {};");
    
    parse("(function f() {});");
    parse("(function () {});");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testReservedKeywords
  public void testReservedKeywords() {
    mode = LanguageMode.ECMASCRIPT3;

    parseError("var boolean;", "identifier is a reserved word");
    parseError("function boolean() {};",
        "identifier is a reserved word");
    parseError("boolean = 1;", "identifier is a reserved word");
    parseError("class = 1;", "identifier is a reserved word");
    parseError("public = 2;", "identifier is a reserved word");

    mode = LanguageMode.ECMASCRIPT5;

    parse("var boolean;");
    parse("function boolean() {};");
    parse("boolean = 1;");
    parseError("class = 1;", "identifier is a reserved word");
    parse("public = 2;");

    mode = LanguageMode.ECMASCRIPT5_STRICT;

    parse("var boolean;");
    parse("function boolean() {};");
    parse("boolean = 1;");
    parseError("class = 1;", "identifier is a reserved word");
    parseError("public = 2;", "identifier is a reserved word");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testKeywordsAsProperties
  public void testKeywordsAsProperties() {
    mode = LanguageMode.ECMASCRIPT3;

    parse("var x = {function: 1};", IRFactory.INVALID_ES3_PROP_NAME);
    parse("x.function;", IRFactory.INVALID_ES3_PROP_NAME);
    parseError("var x = {get x(){} };",
        IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {get function(){} };", IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {get 'function'(){} };",
        IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {get 1(){} };",
        IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {set function(a){} };", IRFactory.SETTER_ERROR_MESSAGE);
    parseError("var x = {set 'function'(a){} };",
        IRFactory.SETTER_ERROR_MESSAGE);
    parseError("var x = {set 1(a){} };",
        IRFactory.SETTER_ERROR_MESSAGE);
    parse("var x = {class: 1};", IRFactory.INVALID_ES3_PROP_NAME);
    parse("var x = {'class': 1};");
    parse("x.class;", IRFactory.INVALID_ES3_PROP_NAME);
    parse("x['class'];");
    parse("var x = {let: 1};");  
    parse("x.let;");
    parse("var x = {yield: 1};"); 
    parse("x.yield;");

    mode = LanguageMode.ECMASCRIPT5;

    parse("var x = {function: 1};");
    parse("x.function;");
    parse("var x = {get function(){} };");
    parse("var x = {get 'function'(){} };");
    parse("var x = {get 1(){} };");
    parse("var x = {set function(a){} };");
    parse("var x = {set 'function'(a){} };");
    parse("var x = {set 1(a){} };");
    parse("var x = {class: 1};");
    parse("x.class;");
    parse("var x = {let: 1};");
    parse("x.let;");
    parse("var x = {yield: 1};");
    parse("x.yield;");

    mode = LanguageMode.ECMASCRIPT5_STRICT;

    parse("var x = {function: 1};");
    parse("x.function;");
    parse("var x = {get function(){} };");
    parse("var x = {get 'function'(){} };");
    parse("var x = {get 1(){} };");
    parse("var x = {set function(a){} };");
    parse("var x = {set 'function'(a){} };");
    parse("var x = {set 1(a){} };");
    parse("var x = {class: 1};");
    parse("x.class;");
    parse("var x = {let: 1};");
    parse("x.let;");
    parse("var x = {yield: 1};");
    parse("x.yield;");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGetPropFunctionName
  public void testGetPropFunctionName() {
    parseError("function a.b() {}",
        "missing ( before function parameters.");
    parseError("var x = function a.b() {}",
        "missing ( before function parameters.");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGetPropFunctionNameIdeMode
  public void testGetPropFunctionNameIdeMode() {
    
    
    isIdeMode = true;
    parseError("function a.b() {}",
        "missing ( before function parameters.",
        "missing formal parameter",
        "missing ) after formal parameters",
        "missing { before function body",
        "syntax error",
        "missing ; before statement",
        "missing ; before statement",
        "missing } after function body",
        "Unsupported syntax: ERROR",
        "Unsupported syntax: ERROR");
    parseError("var x = function a.b() {}",
        "missing ( before function parameters.",
        "missing formal parameter",
        "missing ) after formal parameters",
        "missing { before function body",
        "syntax error",
        "missing ; before statement",
        "missing ; before statement",
        "missing } after function body",
        "Unsupported syntax: ERROR",
        "Unsupported syntax: ERROR");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIdeModePartialTree
  public void testIdeModePartialTree() {
    Node partialTree = parseError("function Foo() {} f.",
        "missing name after . operator");
    assertNull(partialTree);

    isIdeMode = true;
    partialTree = parseError("function Foo() {} f.",
        "missing name after . operator");
    assertNotNull(partialTree);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testForEach
  public void testForEach() {
    parseError(
        "function f(stamp, status) {\n" +
        "  for each ( var curTiming in this.timeLog.timings ) {\n" +
        "    if ( curTiming.callId == stamp ) {\n" +
        "      curTiming.flag = status;\n" +
        "      break;\n" +
        "    }\n" +
        "  }\n" +
        "};",
        "unsupported language extension: for each");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testMisplacedTypeAnnotation1
  public void testMisplacedTypeAnnotation1() {
    
    parse(
        "var o = {};" +
        " o.prop1 = 1, o.prop2 = 2;",
        MISPLACED_TYPE_ANNOTATION);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testMisplacedTypeAnnotation2
  public void testMisplacedTypeAnnotation2() {
    
    parse(
        "var o =  getValue();",
        MISPLACED_TYPE_ANNOTATION);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testMisplacedTypeAnnotation3
  public void testMisplacedTypeAnnotation3() {
    
    parse(
        "var o = 1 +  value;",
        MISPLACED_TYPE_ANNOTATION);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testMisplacedTypeAnnotation4
  public void testMisplacedTypeAnnotation4() {
    
    parse(
        "var o =  ['hello', 'you'];",
        MISPLACED_TYPE_ANNOTATION);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testMisplacedTypeAnnotation5
  public void testMisplacedTypeAnnotation5() {
    
    parse(
        "var o = ( {});",
        MISPLACED_TYPE_ANNOTATION);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testMisplacedTypeAnnotation6
  public void testMisplacedTypeAnnotation6() {
    parse("var o =  function() {return 'str';}",
        MISPLACED_TYPE_ANNOTATION);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testValidTypeAnnotation1
  public void testValidTypeAnnotation1() {
    parse(" var o = 'str';");
    parse("var  o = 'str',  p = 0;");
    parse(" function o() { return 'str'; }");
    parse("var o = {};  o.prop = 'str';");
    parse("var o = {};  o['prop'] = 'str';");
    parse("var o = {  prop : 'str' };");
    parse("var o = {  'prop' : 'str' };");
    parse("var o = {  1 : 'str' };");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testValidTypeAnnotation2
  public void testValidTypeAnnotation2() {
    mode = LanguageMode.ECMASCRIPT5;
    parse("var o = {  get prop() { return 'str' }};");
    parse("var o = {  set prop(s) {}};");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testValidTypeAnnotation3
  public void testValidTypeAnnotation3() {
    
    
    parse("try {} catch ( e) {}");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testParsingAssociativity
  public void testParsingAssociativity() {
    assertNodeEquality(parse("x * y * z"), parse("(x * y) * z"));
    assertNodeEquality(parse("x + y + z"), parse("(x + y) + z"));
    assertNodeEquality(parse("x | y | z"), parse("(x | y) | z"));
    assertNodeEquality(parse("x & y & z"), parse("(x & y) & z"));
    assertNodeEquality(parse("x ^ y ^ z"), parse("(x ^ y) ^ z"));
    
    assertNodeEquality(parse("x || y || z"), parse("x || (y || z)"));
    assertNodeEquality(parse("x && y && z"), parse("x && (y && z)"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIssue1116
  public void testIssue1116() {
    parse("");
  }

// com.google.javascript.rhino.IRTest::testEmpty
  public void testEmpty() {
    testIR(IR.empty(), "EMPTY\n");
  }

// com.google.javascript.rhino.IRTest::testFunction
  public void testFunction() {
    testIR(IR.function(IR.name("hi"), IR.paramList(), IR.block()),
        "FUNCTION hi\n" +
        "    NAME hi\n" +
        "    PARAM_LIST\n" +
        "    BLOCK\n");
  }

// com.google.javascript.rhino.IRTest::testParamList
  public void testParamList() {
    testIR(IR.paramList(),
        "PARAM_LIST\n");

    testIR(IR.paramList(IR.name("a"), IR.name("b")),
        "PARAM_LIST\n" +
        "    NAME a\n" +
        "    NAME b\n");

    testIR(IR.paramList(Lists.newArrayList(IR.name("a"), IR.name("b"))),
        "PARAM_LIST\n" +
        "    NAME a\n" +
        "    NAME b\n");
  }

// com.google.javascript.rhino.IRTest::testBlock
  public void testBlock() {
    testIR(IR.block(),
        "BLOCK\n");

    testIR(IR.block(IR.empty(), IR.empty()),
        "BLOCK\n" +
        "    EMPTY\n" +
        "    EMPTY\n");

    testIR(IR.block(Lists.newArrayList(IR.empty(), IR.empty())),
        "BLOCK\n" +
        "    EMPTY\n" +
        "    EMPTY\n");
  }

// com.google.javascript.rhino.IRTest::testScript
  public void testScript() {
    testIR(IR.script(),
        "SCRIPT\n");

    testIR(IR.script(IR.empty(), IR.empty()),
        "SCRIPT\n" +
        "    EMPTY\n" +
        "    EMPTY\n");

    testIR(IR.script(Lists.newArrayList(IR.empty(), IR.empty())),
        "SCRIPT\n" +
        "    EMPTY\n" +
        "    EMPTY\n");
  }

// com.google.javascript.rhino.IRTest::testScriptThrows
  public void testScriptThrows() {
    boolean caught = false;
    try {
      IR.script(IR.returnNode());
    } catch(IllegalStateException e) {
      caught = true;
    }
    assertTrue("expected exception was not seen", caught);
  }

// com.google.javascript.rhino.IRTest::testVar
  public void testVar() {
    testIR(IR.var(IR.name("a")),
        "VAR\n" +
        "    NAME a\n");

    testIR(IR.var(IR.name("a"), IR.trueNode()),
        "VAR\n" +
        "    NAME a\n" +
        "        TRUE\n");
  }

// com.google.javascript.rhino.IRTest::testReturn
  public void testReturn() {
    testIR(IR.returnNode(),
        "RETURN\n");

    testIR(IR.returnNode(IR.name("a")),
        "RETURN\n" +
        "    NAME a\n");
  }

// com.google.javascript.rhino.IRTest::testThrow
  public void testThrow() {
    testIR(IR.throwNode(IR.name("a")),
        "THROW\n" +
        "    NAME a\n");
  }

// com.google.javascript.rhino.IRTest::testExprResult
  public void testExprResult() {
    testIR(IR.exprResult(IR.name("a")),
        "EXPR_RESULT\n" +
        "    NAME a\n");
  }

// com.google.javascript.rhino.IRTest::testIf
  public void testIf() {
    testIR(IR.ifNode(IR.name("a"), IR.block()),
        "IF\n" +
        "    NAME a\n" +
        "    BLOCK\n");

    testIR(IR.ifNode(IR.name("a"), IR.block(), IR.block()),
        "IF\n" +
        "    NAME a\n" +
        "    BLOCK\n" +
        "    BLOCK\n");
  }

// com.google.javascript.rhino.IRTest::testIssue727_1
  public void testIssue727_1() {
    testIR(
        IR.tryFinally(
            IR.block(),
            IR.block()),
        "TRY\n" +
        "    BLOCK\n" +
        "    BLOCK\n" +
        "    BLOCK\n");
  }

// com.google.javascript.rhino.IRTest::testIssue727_2
  public void testIssue727_2() {
    testIR(
        IR.tryCatch(
            IR.block(),
            IR.catchNode(
                IR.name("e"),
                IR.block())),
        "TRY\n" +
        "    BLOCK\n" +
        "    BLOCK\n" +
        "        CATCH\n" +
        "            NAME e\n" +
        "            BLOCK\n");
  }

// com.google.javascript.rhino.IRTest::testIssue727_3
  public void testIssue727_3() {
    testIR(
        IR.tryCatchFinally(
            IR.block(),
            IR.catchNode(IR.name("e"), IR.block()),
            IR.block()),
        "TRY\n" +
        "    BLOCK\n" +
        "    BLOCK\n" +
        "        CATCH\n" +
        "            NAME e\n" +
        "            BLOCK\n" +
        "    BLOCK\n");
  }

// com.google.javascript.rhino.IRTest::testAdd
  public void testAdd() {
    testIR(
        IR.add(
            IR.cast(IR.number(1)),
            IR.number(2)),
        "ADD\n" +
        "    CAST\n" +
        "        NUMBER 1.0\n" +
        "    NUMBER 2.0\n");

  }

// com.google.javascript.rhino.JSDocInfoTest::testVisibilityOrdinal
  public void testVisibilityOrdinal() {
    assertEquals(0, PRIVATE.ordinal());
    assertEquals(1, PROTECTED.ordinal());
    assertEquals(2, PUBLIC.ordinal());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetType
  public void testSetType() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertTypeEquals(STRING_TYPE, resolve(info.getType()));
    assertNull(info.getVisibility());
    assertTrue(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetTypeAndVisibility
  public void testSetTypeAndVisibility() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("string"));
    info.setVisibility(PROTECTED);

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertTypeEquals(STRING_TYPE, resolve(info.getType()));
    assertEquals(PROTECTED, info.getVisibility());
    assertTrue(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetReturnType
  public void testSetReturnType() {
    JSDocInfo info = new JSDocInfo();
    info.setReturnType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertTypeEquals(STRING_TYPE, resolve(info.getReturnType()));
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetReturnTypeAndBaseType
  public void testSetReturnTypeAndBaseType() {
    JSDocInfo info = new JSDocInfo();
    info.setBaseType(
        new JSTypeExpression(
            new Node(Token.BANG, Node.newString("Number")), ""));
    info.setReturnType(fromString("string"));

    assertTypeEquals(NUMBER_OBJECT_TYPE,
        resolve(info.getBaseType()));
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertTypeEquals(STRING_TYPE, resolve(info.getReturnType()));
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetEnumParameterType
  public void testSetEnumParameterType() {
    JSDocInfo info = new JSDocInfo();
    info.setEnumParameterType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertTypeEquals(STRING_TYPE,
        resolve(info.getEnumParameterType()));
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType
  public void testMultipleSetType() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("number"));

    try {
      info.setReturnType(fromString("boolean"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setEnumParameterType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertTypeEquals(NUMBER_TYPE, resolve(info.getType()));
    assertNull(info.getReturnType());
    assertNull(info.getEnumParameterType());
    assertNull(info.getTypedefType());
    assertTrue(info.hasType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType2
  public void testMultipleSetType2() {
    JSDocInfo info = new JSDocInfo();

    info.setReturnType(fromString("boolean"));

    try {
      info.setType(fromString("number"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setEnumParameterType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertTypeEquals(BOOLEAN_TYPE,
        resolve(info.getReturnType()));
    assertNull(info.getEnumParameterType());
    assertNull(info.getType());
    assertNull(info.getTypedefType());
    assertFalse(info.hasType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType3
  public void testMultipleSetType3() {
    JSDocInfo info = new JSDocInfo();
    info.setEnumParameterType(fromString("boolean"));

    try {
      info.setType(fromString("number"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setReturnType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertNull(info.getType());
    assertNull(info.getTypedefType());
    assertNull(info.getReturnType());
    assertTypeEquals(BOOLEAN_TYPE,
        resolve(info.getEnumParameterType()));
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetTypedefType
  public void testSetTypedefType() {
    JSDocInfo info = new JSDocInfo();
    info.setTypedefType(fromString("boolean"));

    assertTypeEquals(BOOLEAN_TYPE,
        resolve(info.getTypedefType()));
    assertTrue(info.hasTypedefType());
    assertFalse(info.hasType());
    assertFalse(info.hasEnumParameterType());
    assertFalse(info.hasReturnType());
  }
