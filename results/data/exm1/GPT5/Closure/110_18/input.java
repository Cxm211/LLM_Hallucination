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
// com.google.javascript.jscomp.StripCodeTest::testGetElemInIf
  public void testGetElemInIf() {
    test("var logger=null;if(x)logger[f];else foo()", "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testAssignInIf
  public void testAssignInIf() {
    test("var logger=null;if(x)logger=1;else foo()",
         "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testNamePrefix
  public void testNamePrefix() {
    test("a = function(traceZZZ) {}; a.prototype.traceXXX = {x: 1};" +
         "a.prototype.z = function() { this.traceXXX.f(); };" +
         "var traceYYY = 0;",
         "a=function(traceZZZ){};a.prototype.z=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testTypePrefix
  public void testTypePrefix() {
    test("e.f.TraceXXX = function() {}; " +
         "e.f.TraceXXX.prototype.yyy = 2;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames1
  public void testStripCallsToStrippedNames1() {
    test("a = function() { this.logger_ = function(msg){}; };" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
    test("a = function() {};" +
         "a.prototype.logger_ = function(msg) {};" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames2
  public void testStripCallsToStrippedNames2() {
    test("a = function() {};" +
         "a.prototype.logger_ = function(msg) {};" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames3
  public void testStripCallsToStrippedNames3() {
    test("a = function() { this.logger_ = function(msg){}; };" +
         "a.prototype.b = function() { this.logger_('hi').foo = 2; }",
         "a=function(){};a.prototype.b=function(){2;}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames4
  public void testStripCallsToStrippedNames4() {
    test("a = this.logger_().foo;",
         "a = null;");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripVarsInitializedFromStrippedNames1
  public void testStripVarsInitializedFromStrippedNames1() {
    test("a = function() { this.logger_ = function() { return 1; }; };" +
         "a.prototype.b = function() { " +
         "  var one = this.logger_(); if (one) foo() }",
          "a=function(){};a.prototype.b=function(){if(null)foo()}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripVarsInitializedFromStrippedNames2
  public void testStripVarsInitializedFromStrippedNames2() {
    test("a = function() { this.logger_ = function() { return 1; }; };" +
         "a.prototype.b = function() { " +
         "  var one = this.logger_.foo.bar(); if (one) foo() }",
         "a=function(){};a.prototype.b=function(){if(null)foo()}");
  }

// com.google.javascript.jscomp.StripCodeTest::testReportErrorOnStripInNestedAssignment
  public void testReportErrorOnStripInNestedAssignment() {
    
    test("(foo.logger_ = 7) + 8",
         "(foo.logger_ = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);

    
    test("(goog.debug.Logger.foo = 7) + 8",
         "(goog.debug.Logger.foo = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);

    
    test("(GA_GoogleDebugger.foo = 7) + 8",
         "(GA_GoogleDebugger.foo = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testNewOperatior1
  public void testNewOperatior1() {
    test("function foo() {} foo.bar = new goog.debug.Logger();",
         "function foo() {} foo.bar = null;");
  }

// com.google.javascript.jscomp.StripCodeTest::testNewOperatior2
  public void testNewOperatior2() {
    test("function foo() {} foo.bar = (new goog.debug.Logger()).foo();",
         "function foo() {} foo.bar = null;");
  }

// com.google.javascript.jscomp.StripCodeTest::testNewOperatior3
  public void testNewOperatior3() {
    test("(new goog.debug.Logger()).foo().bar = 2;",
         "2;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting1
  public void testCrazyNesting1() {
    test("var x = {}; x[new goog.debug.Logger()] = 3;",
         "var x = {}; x[null] = 3;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting2
  public void testCrazyNesting2() {
    test("var x = {}; x[goog.debug.Logger.getLogger()] = 3;",
         "var x = {}; x[null] = 3;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting3
  public void testCrazyNesting3() {
    test("var x = function() {}; x(new goog.debug.Logger());",
         "var x = function() {}; x(null);");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting4
  public void testCrazyNesting4() {
    test("var x = function() {}; x(goog.debug.Logger.getLogger());",
         "var x = function() {}; x(null);");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting5
  public void testCrazyNesting5() {
    test("var x = function() {}; var y = {}; " +
         "var z = goog.debug.Logger.getLogger(); x(y[z['foo']]);",
         "var x = function() {}; var y = {}; x(y[null]);");
  }

// com.google.javascript.jscomp.StripCodeTest::testNamespace1
  public void testNamespace1() {
    test(
        "var x = {};x.traceutil = {};x.traceutil.FOO = 1;",
        "var x = {};");
  }

// com.google.javascript.jscomp.StripCodeTest::testMethodCallTriggersRemoval
  public void testMethodCallTriggersRemoval() {
    test("this.logger_.foo.bar();",
        "");
  }

// com.google.javascript.jscomp.StripCodeTest::testRemoveExpressionByName
  public void testRemoveExpressionByName() {
    test("this.logger_.foo.bar;",
        "");
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVar
  public void testGlobalVar() throws Exception {
    SymbolTable table = createSymbolTable(
        " var x = 5;");
    assertNull(getGlobalVar(table, "y"));
    assertNotNull(getGlobalVar(table, "x"));
    assertEquals("number", getGlobalVar(table, "x").getType().toString());

    
    assertEquals(2, getVars(table).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences
  public void testGlobalThisReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var x = this; function f() { return this + this + this; }");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(1, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences2
  public void testGlobalThisReferences2() throws Exception {
    
    SymbolTable table = createSymbolTable("");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(0, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences3
  public void testGlobalThisReferences3() throws Exception {
    SymbolTable table = createSymbolTable("this.foo = {}; this.foo.bar = {};");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisPropertyReferences
  public void testGlobalThisPropertyReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function Foo() {} this.Foo;");

    Symbol foo = getGlobalVar(table, "Foo");
    assertNotNull(foo);

    List<Reference> refs = table.getReferenceList(foo);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVarReferences
  public void testGlobalVarReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var x = 5; x = 6;");
    Symbol x = getGlobalVar(table, "x");
    List<Reference> refs = table.getReferenceList(x);

    assertEquals(2, refs.size());
    assertEquals(x.getDeclaration(), refs.get(0));
    assertEquals(Token.VAR, refs.get(0).getNode().getParent().getType());
    assertEquals(Token.ASSIGN, refs.get(1).getNode().getParent().getType());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalVarReferences
  public void testLocalVarReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "function f(x) { return x; }");
    Symbol x = getLocalVar(table, "x");
    List<Reference> refs = table.getReferenceList(x);

    assertEquals(2, refs.size());
    assertEquals(x.getDeclaration(), refs.get(0));
    assertEquals(Token.PARAM_LIST, refs.get(0).getNode().getParent().getType());
    assertEquals(Token.RETURN, refs.get(1).getNode().getParent().getType());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences
  public void testLocalThisReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() { this.foo = 3; this.bar = 5; }");

    Symbol f = getGlobalVar(table, "F");
    assertNotNull(f);

    Symbol t = table.getParameterInFunction(f, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences2
  public void testLocalThisReferences2() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() {}" +
        "F.prototype.baz = " +
        "    function() { this.foo = 3; this.bar = 5; };");

    Symbol baz = getGlobalVar(table, "F.prototype.baz");
    assertNotNull(baz);

    Symbol t = table.getParameterInFunction(baz, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences3
  public void testLocalThisReferences3() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() {}");

    Symbol baz = getGlobalVar(table, "F");
    assertNotNull(baz);

    Symbol t = table.getParameterInFunction(baz, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(0, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testNamespacedReferences
  public void testNamespacedReferences() throws Exception {
    
    
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.dom = {};" +
        "goog.dom.DomHelper = function(){};");
    Symbol goog = getGlobalVar(table, "goog");
    assertNotNull(goog);
    assertEquals(3, Iterables.size(table.getReferences(goog)));

    Symbol googDom = getGlobalVar(table, "goog.dom");
    assertNotNull(googDom);
    assertEquals(2, Iterables.size(table.getReferences(googDom)));

    Symbol googDomHelper = getGlobalVar(table, "goog.dom.DomHelper");
    assertNotNull(googDomHelper);
    assertEquals(1, Iterables.size(table.getReferences(googDomHelper)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testIncompleteNamespacedReferences
  public void testIncompleteNamespacedReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n" +
        "goog.dom.DomHelper = function(){};\n" +
        "var y = goog.dom.DomHelper;\n");
    Symbol goog = getGlobalVar(table, "goog");
    assertNotNull(goog);
    assertEquals(2, table.getReferenceList(goog).size());

    Symbol googDom = getGlobalVar(table, "goog.dom");
    assertNotNull(googDom);
    assertEquals(2, table.getReferenceList(googDom).size());

    Symbol googDomHelper = getGlobalVar(table, "goog.dom.DomHelper");
    assertNotNull(googDomHelper);
    assertEquals(2, Iterables.size(table.getReferences(googDomHelper)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalRichObjectReference
  public void testGlobalRichObjectReference() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n" +
        "function A(){};\n" +
        " A.prototype.b;\n" +
        " var a = new A();\n" +
        "function g() {\n" +
        "  return a.b ? 'x' : 'y';\n" +
        "}\n" +
        "(function() {\n" +
        "  var x; if (x) { x = a.b.b; } else { x = a.b.c; }\n" +
        "  return x;\n" +
        "})();\n");

    Symbol ab = getGlobalVar(table, "a.b");
    assertNull(ab);

    Symbol propB = getGlobalVar(table, "A.prototype.b");
    assertNotNull(propB);
    assertEquals(5, table.getReferenceList(propB).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testRemovalOfNamespacedReferencesOfProperties
  public void testRemovalOfNamespacedReferencesOfProperties()
      throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        " DomHelper.method = function() {};");

    Symbol domHelper = getGlobalVar(table, "DomHelper");
    assertNotNull(domHelper);

    Symbol domHelperNamespacedMethod = getGlobalVar(table, "DomHelper.method");
    assertEquals("method", domHelperNamespacedMethod.getName());

    Symbol domHelperMethod = domHelper.getPropertyScope().getSlot("method");
    assertNotNull(domHelperMethod);
  }

// com.google.javascript.jscomp.SymbolTableTest::testGoogScopeReferences
  public void testGoogScopeReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.scope = function() {};" +
        "goog.scope(function() {});");
    Symbol googScope = getGlobalVar(table, "goog.scope");
    assertNotNull(googScope);
    assertEquals(2, Iterables.size(table.getReferences(googScope)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGoogRequireReferences
  public void testGoogRequireReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.provide = function() {};" +
        "goog.require = function() {};" +
        "goog.provide('goog.dom');" +
        "goog.require('goog.dom');");
    Symbol goog = getGlobalVar(table, "goog");
    assertNotNull(goog);

    
    
    
    
    
    
    
    
    assertEquals(8, Iterables.size(table.getReferences(goog)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGoogRequireReferences2
  public void testGoogRequireReferences2() throws Exception {
    options.brokenClosureRequiresLevel = CheckLevel.OFF;
    SymbolTable table = createSymbolTable(
        "foo.bar = function(){};  
        + "goog.require('foo.bar')\n");
    Symbol fooBar = getGlobalVar(table, "foo.bar");
    assertNotNull(fooBar);
    assertEquals(2, Iterables.size(table.getReferences(fooBar)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVarInExterns
  public void testGlobalVarInExterns() throws Exception {
    SymbolTable table = createSymbolTable("customExternFn(1);");
    Symbol fn = getGlobalVar(table, "customExternFn");
    List<Reference> refs = table.getReferenceList(fn);
    assertEquals(2, refs.size());

    SymbolScope scope = table.getEnclosingScope(refs.get(0).getNode());
    assertTrue(scope.isGlobalScope());
    assertEquals(SymbolTable.GLOBAL_THIS,
        table.getSymbolForScope(scope).getName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalVarInExterns
  public void testLocalVarInExterns() throws Exception {
    SymbolTable table = createSymbolTable("");
    Symbol arg = getLocalVar(table, "customExternArg");
    List<Reference> refs = table.getReferenceList(arg);
    assertEquals(1, refs.size());

    Symbol fn = getGlobalVar(table, "customExternFn");
    SymbolScope scope = table.getEnclosingScope(refs.get(0).getNode());
    assertFalse(scope.isGlobalScope());
    assertEquals(fn, table.getSymbolForScope(scope));
  }

// com.google.javascript.jscomp.SymbolTableTest::testSymbolsForType
  public void testSymbolsForType() throws Exception {
    SymbolTable table = createSymbolTable(
        "function random() { return 1; }" +
        " function Foo() {}" +
        " function Bar() {}" +
        "var x = random() ? new Foo() : new Bar();");

    Symbol x = getGlobalVar(table, "x");
    Symbol foo = getGlobalVar(table, "Foo");
    Symbol bar = getGlobalVar(table, "Bar");
    Symbol fooPrototype = getGlobalVar(table, "Foo.prototype");
    Symbol fn = getGlobalVar(table, "Function");
    assertEquals(
        Lists.newArrayList(foo, bar), table.getAllSymbolsForTypeOf(x));
    assertEquals(
        Lists.newArrayList(fn), table.getAllSymbolsForTypeOf(foo));
    assertEquals(
        Lists.newArrayList(foo), table.getAllSymbolsForTypeOf(fooPrototype));
    assertEquals(
        foo,
        table.getSymbolDeclaredBy(
            foo.getType().toMaybeFunctionType()));
  }

// com.google.javascript.jscomp.SymbolTableTest::testStaticMethodReferences
  public void testStaticMethodReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        " DomHelper.method = function() {};" +
        "function f() { var x = DomHelper; x.method() + x.method(); }");

    Symbol method =
        getGlobalVar(table, "DomHelper").getPropertyScope().getSlot("method");
    assertEquals(
        3, Iterables.size(table.getReferences(method)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodReferences
  public void testMethodReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        " DomHelper.prototype.method = function() {};" +
        "function f() { " +
        "  (new DomHelper()).method(); (new DomHelper()).method(); };");

    Symbol method =
        getGlobalVar(table, "DomHelper.prototype.method");
    assertEquals(
        3, Iterables.size(table.getReferences(method)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testSuperClassMethodReferences
  public void testSuperClassMethodReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.inherits = function(a, b) {};" +
        " var A = function(){};" +
        " A.prototype.method = function() {};" +
        "\n" +
        "var B = function(){};\n" +
        "goog.inherits(B, A);" +
        " B.prototype.method = function() {" +
        "  B.superClass_.method();" +
        "};");

    Symbol methodA =
        getGlobalVar(table, "A.prototype.method");
    assertEquals(
        2, Iterables.size(table.getReferences(methodA)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodReferencesMissingTypeInfo
  public void testMethodReferencesMissingTypeInfo() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};\n" +
        " DomHelper.prototype.method = function() {\n" +
        "  this.method();\n" +
        "};\n" +
        "function f() { " +
        "  (new DomHelper()).method();\n" +
        "};");

    Symbol method =
        getGlobalVar(table, "DomHelper.prototype.method");
    assertEquals(
        3, Iterables.size(table.getReferences(method)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testFieldReferencesMissingTypeInfo
  public void testFieldReferencesMissingTypeInfo() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){ this.prop = 1; };\n" +
        " DomHelper.prototype.prop = 2;\n" +
        "function f() {\n" +
        "  return (new DomHelper()).prop;\n" +
        "};");

    Symbol prop =
        getGlobalVar(table, "DomHelper.prototype.prop");
    assertEquals(3, table.getReferenceList(prop).size());

    assertNull(getLocalVar(table, "this.prop"));
  }

// com.google.javascript.jscomp.SymbolTableTest::testFieldReferences
  public void testFieldReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){" +
        "   this.field = 3;" +
        "};" +
        "function f() { " +
        "  return (new DomHelper()).field + (new DomHelper()).field; };");

    Symbol field = getGlobalVar(table, "DomHelper.prototype.field");
    assertEquals(
        3, Iterables.size(table.getReferences(field)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testUndeclaredFieldReferences
  public void testUndeclaredFieldReferences() throws Exception {
    
    
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        "DomHelper.prototype.method = function() { " +
        "  this.field = 3;" +
        "  return x.field;" +
        "}");

    Symbol field = getGlobalVar(table, "DomHelper.prototype.field");
    assertNull(field);
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences
  public void testPrototypeReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function DomHelper() {}" +
        "DomHelper.prototype.method = function() {};");
    Symbol prototype =
        getGlobalVar(table, "DomHelper.prototype");
    assertNotNull(prototype);

    List<Reference> refs = table.getReferenceList(prototype);

    
    assertEquals(refs.toString(), 2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences2
  public void testPrototypeReferences2() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n"
        + "function Snork() {}\n"
        + "Snork.prototype.baz = 3;\n");
    Symbol prototype =
        getGlobalVar(table, "Snork.prototype");
    assertNotNull(prototype);

    List<Reference> refs = table.getReferenceList(prototype);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences3
  public void testPrototypeReferences3() throws Exception {
    SymbolTable table = createSymbolTable(
        " function Foo() {}");
    Symbol fooPrototype = getGlobalVar(table, "Foo.prototype");
    assertNotNull(fooPrototype);

    List<Reference> refs = table.getReferenceList(fooPrototype);
    assertEquals(1, refs.size());
    assertEquals(Token.NAME, refs.get(0).getNode().getType());

    
    
    assertEquals(
        refs.get(0).getNode(),
        table.getReferenceList(getGlobalVar(table, "Foo")).get(0).getNode());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences4
  public void testPrototypeReferences4() throws Exception {
    SymbolTable table = createSymbolTable(
        " function Foo() {}" +
        "Foo.prototype = {bar: 3}");
    Symbol fooPrototype = getGlobalVar(table, "Foo.prototype");
    assertNotNull(fooPrototype);

    List<Reference> refs = Lists.newArrayList(
        table.getReferences(fooPrototype));
    assertEquals(1, refs.size());
    assertEquals(Token.GETPROP, refs.get(0).getNode().getType());
    assertEquals("Foo.prototype", refs.get(0).getNode().getQualifiedName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences5
  public void testPrototypeReferences5() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};  goog.Foo = function() {};");
    Symbol fooPrototype = getGlobalVar(table, "goog.Foo.prototype");
    assertNotNull(fooPrototype);

    List<Reference> refs = table.getReferenceList(fooPrototype);
    assertEquals(1, refs.size());
    assertEquals(Token.GETPROP, refs.get(0).getNode().getType());

    
    
    assertEquals(
        refs.get(0).getNode(),
        table.getReferenceList(
            getGlobalVar(table, "goog.Foo")).get(0).getNode());
  }

// com.google.javascript.jscomp.SymbolTableTest::testReferencesInJSDocType
  public void testReferencesInJSDocType() {
    SymbolTable table = createSymbolTable(
        " function Foo() {}\n" +
        " var x;\n" +
        " function f(x) {}\n" +
        " function g() {}\n" +
        " function Sub() {}");
    Symbol foo = getGlobalVar(table, "Foo");
    assertNotNull(foo);

    List<Reference> refs = table.getReferenceList(foo);
    assertEquals(5, refs.size());

    assertEquals(1, refs.get(0).getNode().getLineno());
    assertEquals(29, refs.get(0).getNode().getCharno());
    assertEquals(3, refs.get(0).getNode().getLength());

    assertEquals(2, refs.get(1).getNode().getLineno());
    assertEquals(11, refs.get(1).getNode().getCharno());

    assertEquals(3, refs.get(2).getNode().getLineno());
    assertEquals(12, refs.get(2).getNode().getCharno());

    assertEquals(4, refs.get(3).getNode().getLineno());
    assertEquals(25, refs.get(3).getNode().getCharno());

    assertEquals(7, refs.get(4).getNode().getLineno());
    assertEquals(13, refs.get(4).getNode().getCharno());
  }

// com.google.javascript.jscomp.SymbolTableTest::testReferencesInJSDocType2
  public void testReferencesInJSDocType2() {
    SymbolTable table = createSymbolTable(
        " function f(x) {}\n");
    Symbol str = getGlobalVar(table, "String");
    assertNotNull(str);

    List<Reference> refs = table.getReferenceList(str);

    
    
    
    
    assertTrue(refs.size() > 1);

    int last = refs.size() - 1;
    for (int i = 0; i < refs.size(); i++) {
      Reference ref = refs.get(i);
      assertEquals(i != last, ref.getNode().isFromExterns());
      if (!ref.getNode().isFromExterns()) {
        assertEquals("in1", ref.getNode().getSourceFileName());
      }
    }
  }

// com.google.javascript.jscomp.SymbolTableTest::testDottedReferencesInJSDocType
  public void testDottedReferencesInJSDocType() {
    SymbolTable table = createSymbolTable(
        "var goog = {};\n" +
        " goog.Foo = function() {}\n" +
        " var x;\n" +
        " function f(x) {}\n" +
        " function g() {}\n" +
        " function Sub() {}");
    Symbol foo = getGlobalVar(table, "goog.Foo");
    assertNotNull(foo);

    List<Reference> refs = table.getReferenceList(foo);
    assertEquals(5, refs.size());

    assertEquals(2, refs.get(0).getNode().getLineno());
    assertEquals(20, refs.get(0).getNode().getCharno());
    assertEquals(8, refs.get(0).getNode().getLength());

    assertEquals(3, refs.get(1).getNode().getLineno());
    assertEquals(11, refs.get(1).getNode().getCharno());

    assertEquals(4, refs.get(2).getNode().getLineno());
    assertEquals(12, refs.get(2).getNode().getCharno());

    assertEquals(5, refs.get(3).getNode().getLineno());
    assertEquals(25, refs.get(3).getNode().getCharno());

    assertEquals(8, refs.get(4).getNode().getLineno());
    assertEquals(13, refs.get(4).getNode().getCharno());
  }

// com.google.javascript.jscomp.SymbolTableTest::testReferencesInJSDocName
  public void testReferencesInJSDocName() {
    String code = " function f(x) {}\n";
    SymbolTable table = createSymbolTable(code);
    Symbol x = getLocalVar(table, "x");
    assertNotNull(x);

    List<Reference> refs = table.getReferenceList(x);
    assertEquals(2, refs.size());

    assertEquals(code.indexOf("x) {"), refs.get(0).getNode().getCharno());
    assertEquals(code.indexOf("x */"), refs.get(1).getNode().getCharno());
    assertEquals("in1",
        refs.get(0).getNode().getSourceFileName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalQualifiedNamesInLocalScopes
  public void testLocalQualifiedNamesInLocalScopes() {
    SymbolTable table = createSymbolTable(
        "function f() { var x = {}; x.number = 3; }");
    Symbol xNumber = getLocalVar(table, "x.number");
    assertNotNull(xNumber);
    assertFalse(table.getScope(xNumber).isGlobalScope());

    assertEquals("number", xNumber.getType().toString());
  }

// com.google.javascript.jscomp.SymbolTableTest::testNaturalSymbolOrdering
  public void testNaturalSymbolOrdering() {
    SymbolTable table = createSymbolTable(
        " var a = {};" +
        " a.b = {};" +
        " function f(x) {}");
    Symbol a = getGlobalVar(table, "a");
    Symbol ab = getGlobalVar(table, "a.b");
    Symbol f = getGlobalVar(table, "f");
    Symbol x = getLocalVar(table, "x");
    Ordering<Symbol> ordering = table.getNaturalSymbolOrdering();
    assertSymmetricOrdering(ordering, a, ab);
    assertSymmetricOrdering(ordering, a, f);
    assertSymmetricOrdering(ordering, f, ab);
    assertSymmetricOrdering(ordering, f, x);
  }

// com.google.javascript.jscomp.SymbolTableTest::testDeclarationDisagreement
  public void testDeclarationDisagreement() {
    SymbolTable table = createSymbolTable(
        " var goog = goog || {};\n" +
        "\n" +
        "goog.addSingletonGetter2 = function(x) {};\n" +
        "\n" +
        "goog.addSingletonGetter = goog.addSingletonGetter2;\n" +
        "\n" +
        "goog.addSingletonGetter = function(x) {};\n");

    Symbol method = getGlobalVar(table, "goog.addSingletonGetter");
    List<Reference> refs = table.getReferenceList(method);
    assertEquals(2, refs.size());

    
    assertEquals(7, method.getDeclaration().getNode().getLineno());
    assertEquals(5, refs.get(1).getNode().getLineno());
  }

// com.google.javascript.jscomp.SymbolTableTest::testMultipleExtends
  public void testMultipleExtends() {
    SymbolTable table = createSymbolTable(
        " var goog = goog || {};\n" +
        "goog.inherits = function(x, y) {};\n" +
        "\n" +
        "goog.A = function() { this.fieldA = this.constructor; };\n" +
        " goog.A.FooA = function() {};\n" +
        " goog.A.prototype.methodA = function() {};\n" +
        "\n" +
        "goog.B = function() { this.fieldB = this.constructor; };\n" +
        "goog.inherits(goog.B, goog.A);\n" +
        " goog.B.prototype.methodB = function() {};\n" +
        "\n" +
        "goog.B2 = function() { this.fieldB = this.constructor; };\n" +
        "goog.inherits(goog.B2, goog.A);\n" +
        " goog.B2.FooB = function() {};\n" +
        " goog.B2.prototype.methodB = function() {};\n" +
        "\n" +
        "goog.C = function() { this.fieldC = this.constructor; };\n" +
        "goog.inherits(goog.C, goog.B);\n" +
        " goog.C.FooC = function() {};\n" +
        " goog.C.prototype.methodC = function() {};\n");

    Symbol bCtor = getGlobalVar(table, "goog.B.prototype.constructor");
    assertNotNull(bCtor);

    List<Reference> bRefs = table.getReferenceList(bCtor);
    assertEquals(2, bRefs.size());
    assertEquals(11, bCtor.getDeclaration().getNode().getLineno());

    Symbol cCtor = getGlobalVar(table, "goog.C.prototype.constructor");
    assertNotNull(cCtor);

    List<Reference> cRefs = table.getReferenceList(cCtor);
    assertEquals(2, cRefs.size());
    assertEquals(26, cCtor.getDeclaration().getNode().getLineno());
  }

// com.google.javascript.jscomp.SymbolTableTest::testJSDocAssociationWithBadNamespace
  public void testJSDocAssociationWithBadNamespace() {
    SymbolTable table = createSymbolTable(
        
        
        
        " goog.Foo = function(){};");

    Symbol foo = getGlobalVar(table, "goog.Foo");
    assertNotNull(foo);

    JSDocInfo info = foo.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isConstructor());
  }

// com.google.javascript.jscomp.SymbolTableTest::testMissingConstructorTag
  public void testMissingConstructorTag() {
    SymbolTable table = createSymbolTable(
        "function F() {" +
        "  this.field1 = 3;" +
        "}" +
        "F.prototype.method1 = function() {" +
        "  this.field1 = 5;" +
        "};" +
        "(new F()).method1();");

    
    
    assertNull(getGlobalVar(table, "F.prototype.field1"));

    Symbol sym = getGlobalVar(table, "F.prototype.method1");
    assertEquals(1, table.getReferenceList(sym).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testTypeCheckingOff
  public void testTypeCheckingOff() {
    options = new CompilerOptions();

    
    SymbolTable table = createSymbolTable(
        "" +
        "function F() {" +
        "  this.field1 = 3;" +
        "}" +
        "F.prototype.method1 = function() {" +
        "  this.field1 = 5;" +
        "};" +
        "(new F()).method1();");
    assertNull(getGlobalVar(table, "F.prototype.field1"));
    assertNull(getGlobalVar(table, "F.prototype.method1"));

    Symbol sym = getGlobalVar(table, "F");
    assertEquals(3, table.getReferenceList(sym).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testSuperClassReference
  public void testSuperClassReference() throws Exception {
    SymbolTable table = createSymbolTable(
        "  var a = {b: {}};\n"
        + "\n"
        + "a.b.BaseClass = function() {};\n"
        + "a.b.BaseClass.prototype.doSomething = function() {\n"
        + "  alert('hi');\n"
        + "};\n"
        + "\n"
        + "a.b.DerivedClass = function() {};\n"
        + "goog.inherits(a.b.DerivedClass, a.b.BaseClass);\n"
        + "\n"
        + "a.b.DerivedClass.prototype.doSomething = function() {\n"
        + "  a.b.DerivedClass.superClass_.doSomething();\n"
        + "};\n");

    Symbol bad = getGlobalVar(
        table, "a.b.DerivedClass.superClass_.doSomething");
    assertNull(bad);

    Symbol good = getGlobalVar(
        table, "a.b.BaseClass.prototype.doSomething");
    assertNotNull(good);

    List<Reference> refs = table.getReferenceList(good);
    assertEquals(2, refs.size());
    assertEquals("a.b.DerivedClass.superClass_.doSomething",
        refs.get(1).getNode().getQualifiedName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testInnerEnum
  public void testInnerEnum() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {}; goog.ui = {};"
        + "  \n"
        + "goog.ui.Zippy = function() {};\n"
        + "\n"
        + "goog.ui.Zippy.EventType = { TOGGLE: 'toggle' };\n");

    Symbol eventType = getGlobalVar(table, "goog.ui.Zippy.EventType");
    assertNotNull(eventType);
    assertTrue(eventType.getType().isEnumType());

    Symbol toggle = getGlobalVar(table, "goog.ui.Zippy.EventType.TOGGLE");
    assertNotNull(toggle);
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodInAnonObject1
  public void testMethodInAnonObject1() throws Exception {
    SymbolTable table = createSymbolTable(
        "var a = {}; a.b = {}; a.b.c = function() {};");
    Symbol a = getGlobalVar(table, "a");
    Symbol ab = getGlobalVar(table, "a.b");
    Symbol abc = getGlobalVar(table, "a.b.c");

    assertNotNull(abc);
    assertEquals(1, table.getReferenceList(abc).size());

    assertEquals("{b: {c: function (): undefined}}", a.getType().toString());
    assertEquals("{c: function (): undefined}", ab.getType().toString());
    assertEquals("function (): undefined", abc.getType().toString());
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodInAnonObject2
  public void testMethodInAnonObject2() throws Exception {
    SymbolTable table = createSymbolTable(
        "var a = {b: {c: function() {}}};");
    Symbol a = getGlobalVar(table, "a");
    Symbol ab = getGlobalVar(table, "a.b");
    Symbol abc = getGlobalVar(table, "a.b.c");

    assertNotNull(abc);
    assertEquals(1, table.getReferenceList(abc).size());

    assertEquals("{b: {c: function (): undefined}}", a.getType().toString());
    assertEquals("{c: function (): undefined}", ab.getType().toString());
    assertEquals("function (): undefined", abc.getType().toString());
  }

// com.google.javascript.jscomp.SymbolTableTest::testJSDocOnlySymbol
  public void testJSDocOnlySymbol() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n"
        + "var a;");
    Symbol x = getDocVar(table, "x");
    assertNotNull(x);
    assertEquals("number", x.getType().toString());
    assertEquals(1, table.getReferenceList(x).size());

    Symbol y = getDocVar(table, "y");
    assertNotNull(x);
    assertEquals(null, y.getType());
    assertEquals(1, table.getReferenceList(y).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testNamespaceDefinitionOrder
  public void testNamespaceDefinitionOrder() throws Exception {
    
    
    SymbolTable table = createSymbolTable(
        " var goog = {};\n"
        + " goog.dom.Foo = function() {};\n"
        + " goog.dom = {};\n");

    Symbol goog = getGlobalVar(table, "goog");
    Symbol dom = getGlobalVar(table, "goog.dom");
    Symbol Foo = getGlobalVar(table, "goog.dom.Foo");

    assertNotNull(goog);
    assertNotNull(dom);
    assertNotNull(Foo);

    assertEquals(dom, goog.getPropertyScope().getSlot("dom"));
    assertEquals(Foo, dom.getPropertyScope().getSlot("Foo"));
  }

// com.google.javascript.jscomp.SymbolTableTest::testConstructorAlias
  public void testConstructorAlias() throws Exception {
    SymbolTable table = createSymbolTable(
        " var Foo = function() {};\n" +
        " Foo.prototype.bar = function() {};\n" +
        " var FooAlias = Foo;\n" +
        " FooAlias.prototype.baz = function() {};\n");

    Symbol foo = getGlobalVar(table, "Foo");
    Symbol fooAlias = getGlobalVar(table, "FooAlias");
    Symbol bar = getGlobalVar(table, "Foo.prototype.bar");
    Symbol baz = getGlobalVar(table, "Foo.prototype.baz");
    Symbol bazAlias = getGlobalVar(table, "FooAlias.prototype.baz");

    assertNotNull(foo);
    assertNotNull(fooAlias);
    assertNotNull(bar);
    assertNotNull(baz);
    assertNull(bazAlias);

    Symbol barScope = table.getSymbolForScope(table.getScope(bar));
    assertNotNull(barScope);

    Symbol bazScope = table.getSymbolForScope(table.getScope(baz));
    assertNotNull(bazScope);

    Symbol fooPrototype = foo.getPropertyScope().getSlot("prototype");
    assertNotNull(fooPrototype);

    assertEquals(fooPrototype, barScope);
    assertEquals(fooPrototype, bazScope);
  }

// com.google.javascript.jscomp.SymbolTableTest::testSymbolForScopeOfNatives
  public void testSymbolForScopeOfNatives() throws Exception {
    SymbolTable table = createSymbolTable("");

    
    Symbol sliceArg = getLocalVar(table, "sliceArg");
    assertNotNull(sliceArg);

    Symbol scope = table.getSymbolForScope(table.getScope(sliceArg));
    assertNotNull(scope);
    assertEquals(scope, getGlobalVar(table, "String.prototype.slice"));

    Symbol proto = getGlobalVar(table, "String.prototype");
    assertEquals(
        "externs1", proto.getDeclaration().getNode().getSourceFileName());
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testFunctionScope
  public void testFunctionScope() {
    Scope scope = getScope("function foo() {}\n" +
                           "var x = function bar(a1) {};" +
                           "[function bar2() { var y; }];" +
                           "if (true) { function z() {} }"
                           );
    assertTrue(scope.isDeclared("foo", false));
    assertTrue(scope.isDeclared("x", false));
    assertTrue(scope.isDeclared("z", false));

    
    assertFalse(scope.isDeclared("a1", false));
    assertFalse(scope.isDeclared("bar", false));
    assertFalse(scope.isDeclared("bar2", false));
    assertFalse(scope.isDeclared("y", false));
    assertFalse(scope.isDeclared("", false));
  }

// com.google.javascript.jscomp.SyntacticScopeCreatorTest::testScopeRootNode
  public void testScopeRootNode() {
    String js = "function foo() {\n" +
        " var x = 10;" +
        "}";
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());

    Scope globalScope =
        new SyntacticScopeCreator(compiler).createScope(root, null);
    assertEquals(root, globalScope.getRootNode());

    Node fooNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fooNode.getType());
    Scope fooScope =
        new SyntacticScopeCreator(compiler).createScope(fooNode, null);
    assertEquals(fooNode, fooScope.getRootNode());
    assertTrue(fooScope.isDeclared("x", false));
  }

// com.google.javascript.jscomp.TightenTypesTest::testTopLevelVariables
  public void testTopLevelVariables() {
    testSame(" function Foo() {}\n"
             + "var a = new Foo();\n"
             + "var b = a;\n");

    assertTrue(getType("Foo").isFunction());
    assertTrue(getType("a").isInstance());
    assertType("function (this:Foo): ()", getType("Foo"));
    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));

    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = new Foo();\n"
             + "a = new Bar();\n"
             + "var b = a;\n");

    assertTrue(getType("a").isUnion());
    assertType("(Bar,Foo)", getType("a"));
    assertType("Bar", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testNamespacedVariables
  public void testNamespacedVariables() {
    testSame("var goog = goog || {}; goog.foo = {};\n"
             + " goog.foo.Foo = function() {};\n"
             + "goog.foo.Foo.prototype.blah = function() {};\n"
             + " goog.foo.Bar = function() {};\n"
             + "goog.foo.Bar.prototype.blah = function() {};\n"
             + "function bar(a) { a.blah(); }\n"
             + "var baz = bar;\n"
             + "bar(new goog.foo.Foo);\n"
             + "baz(new goog.foo.Bar);\n");

    assertType("(goog.foo.Bar,goog.foo.Foo)", getParamType(getType("bar"), 0));
    assertType("(goog.foo.Bar,goog.foo.Foo)", getParamType(getType("baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testReturnSlot
  public void testReturnSlot() {
    testSame(" function Foo() {}\n"
             + "function bar() {\n"
             + "  var a = new Foo();\n"
             + "  return a;\n"
             + "}\n"
             + "var b = bar();\n");

    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testParameterSlots
  public void testParameterSlots() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "function bar(a, b) {}\n"
             + "bar(new Foo, new Foo);\n"
             + "bar(new Bar, null);\n");

    assertType("(Bar,Foo)", getParamType(getType("bar"), 0));
    assertType("Foo", getParamType(getType("bar"), 1));
    assertNull(getParamVar(getType("bar"), 2));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAliasedFunction
  public void testAliasedFunction() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "function bar(a) {}\n"
             + "var baz = bar;\n"
             + "bar(new Foo);\n"
             + "baz(new Bar);\n");

    assertType("(Bar,Foo)", getParamType(getType("bar"), 0));
    assertType("(Bar,Foo)", getParamType(getType("baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCatchStatement
  public void testCatchStatement() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
             " function Bar() {}\n"
             + "function bar() { try { } catch (e) { return e; } }\n"
             + " function ID10TError() {}\n"
             + "var a = bar(); throw new ID10TError();\n", null, null);

    assertType("(Error,EvalError,ID10TError,RangeError,ReferenceError,"
        + "SyntaxError,TypeError,URIError)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testConstructorParameterSlots
  public void testConstructorParameterSlots() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz(a) {}\n"
             + "new Baz(new Foo);\n"
             + "new Baz(new Bar);\n");

    assertType("(Bar,Foo)", getParamType(getType("Baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallSlot
  public void testCallSlot() {
    testSame("function foo() {}\n"
             + "function bar() {}\n"
             + "function baz() {}\n"
             + "var a = foo;\n"
             + "a = bar;\n"
             + "a();\n");

    assertTrue(isCalled(getType("foo")));
    assertTrue(isCalled(getType("bar")));
    assertFalse(isCalled(getType("baz")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testObjectLiteralTraversal
  public void testObjectLiteralTraversal() {
    testSame("var foo = function() {}\n"
             + "function bar() { return { 'a': foo()} };\n"
             + "bar();");
    assertTrue(isCalled(getType("foo")));
   }

// com.google.javascript.jscomp.TightenTypesTest::testThis
  public void testThis() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this; }\n"
             + "var a = new Foo();\n"
             + "var b = a.foo();\n");

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAssign
  public void testAssign() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = new Foo();\n"
             + "var b = a = new Bar();\n");

    assertType("(Bar,Foo)", getType("a"));
    assertType("Bar", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testComma
  public void testComma() {
    testSame(" function Foo() {b=new Foo()}\n"
             + "var b;"
             + " function Bar() {}\n"
             + "var a = (new Foo, new Bar);\n");

    assertType("Bar", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAnd
  public void testAnd() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = (new Foo && new Bar);\n");

    assertType("Bar", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testOr
  public void testOr() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " var f = new Foo();\n"
             + " var b = new Bar();\n"
             + "var a = (f || b);\n");

    assertType("(Bar,Foo)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testHook
  public void testHook() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = (1+1 == 2) ? new Foo : new Bar;\n");

    assertType("(Bar,Foo)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testFunctionLiteral
  public void testFunctionLiteral() {
    testSame(" function Foo() {}\n"
             + "var a = (function() { return new Foo; })();\n");

    assertType("Foo", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testNameLookup
  public void testNameLookup() {
    testSame(" function Foo() {}\n"
             + "var a = new Foo;\n"
             + "var b = (function() { return a; })();\n");

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetProp
  public void testGetProp() {
    testSame(" function Foo() {\n"
             + "  this.foo = new A();\n"
             + "}\n"
             + " function Bar() {\n"
             + "  this.foo = new B();\n"
             + "}\n"
             + " function Baz() {}\n"
             + " function A() {}\n"
             + " function B() {}\n"
             
             + " var foo = new Foo();\n"
             + " var bar = new Bar();\n"
             + " var baz = new Baz();\n" 
             + "var a = foo || bar || baz\n"
             + "var b = a.foo;\n");

    assertType("(A,B)", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetPrototypeProperty
  public void testGetPrototypeProperty() {
    testSame(" function Foo() {};\n"
             + " function Bar() {};\n"
             + "Bar.prototype.a = new Foo();\n"
             + "var a = Bar.prototype.a;\n");

    assertType("Foo", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetElem
  public void testGetElem() {
    testSame(
        "\n"
        + "function Array(var_args) {}\n",
        " function Foo() {}\n"
        + " function Bar() {}\n"
        + "var a = [];\n"
        + "a[0] = new Foo;\n"
        + "a[1] = new Bar;\n"
        + "var b = a[0];\n"
        + "var c = [new Foo, new Bar];\n", null);

    assertType("Array", getType("a"));
    assertType("(Array,Bar,Foo)", getType("b"));
    assertType("Array", getType("c"));

    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz() {\n"
             + "  this.arr = [];\n"
             + "}\n"
             + "var b = new Baz;\n"
             + "b.arr[0] = new Foo;\n"
             + "b.arr[1] = new Bar;\n"
             + "var c = b.arr;\n");

    assertType("Array", getType("c"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetElem3
  public void testGetElem3() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
             " function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz() {\n"
             + "  this.arr = [];\n"
             + "}\n"
             + "function foo(anarr) {"
             + "}\n"
             + "var ar = [];\n"
             + "foo(ar);\n", null);

    assertType("Array", getType("ar"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testScopeDiscovery
  public void testScopeDiscovery() {
    testSame("function spam() {}\n"
             + "function foo() {}\n"
             + "function bar() {\n"
             + "  return function() { foo(); };\n"
             + "}"
             + "function baz() {\n"
             + "  return function() { bar()(); };\n"
             + "}"
             + "baz()()();\n");

    assertFalse(isCalled(getType("spam")));
    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSheqDiscovery
  public void testSheqDiscovery() {
    testSame("function spam() {}\n"
             + "\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo1 = function() { f1(); }\n"
             + "Foo.prototype.foo2 = function() { f2(); }\n"
             + "Foo.prototype.foo3 = function() { f3(); }\n"
             + "function baz(a) {\n"
             + "  a === null || a instanceof Foo ?\n"
             + "  Foo.prototype.foo1.call(this) :\n"
             + "  Foo.prototype.foo2.call(this);\n"
             + "}\n"
             + "function f1() {}\n"
             + "function f2() {}\n"
             + "function f3() {}\n"
             + "baz(3);\n");

    assertFalse(isCalled(getType("spam")));
    assertFalse(isCalled(getType("f3")));
    assertTrue(isCalled(getType("f1")));
    assertTrue(isCalled(getType("f2")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSubclass
  public void testSubclass() {
    testSame("\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this.bar; };\n"
             + "Foo.prototype.bar = function() { return new A(); };\n"
             + "\n"
             + "function Bar() {}\n"
             + "\n"
             + "Bar.prototype.bar = function() { return new B(); };\n"
             + " function A() {}\n"
             + " function B() {}\n"
             + "var a = (new Foo()).foo()();\n"
             + "a = (new Bar()).foo()();\n");

    ConcreteType fooType =
        getPropertyType(getFunctionPrototype(getType("Foo")), "foo");
    assertType("(Bar,Foo)", getThisType(fooType));
    assertType("(A,B)", getType("a"));

    testSame("\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this.bar; };\n"
             + "Foo.prototype.bar = function() { return new A(); };\n"
             + "\n"
             + "function Bar() {}\n"
             + "\n"
             + "Bar.prototype.bar = function() { return new B(); };\n"
             + " function A() {}\n"
             + " function B() {}\n"
             + "var a = (new Bar()).foo()();\n");

    fooType = getPropertyType(getFunctionPrototype(getType("Foo")), "foo");
    assertType("Bar", getThisType(fooType));
    assertType("B", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testArrayAssignments
  public void testArrayAssignments() {
    testSame(" function Foo() {}\n"
             + "var a = [];\n"
             + "function foo() { return []; }\n"
             + "(a.length == 0 ? a : foo())[0] = new Foo;\n"
             + "var b = a[0];\n"
             + "var c = foo()[0];\n");

    assertType("(Array,Foo)", getType("b"));
    assertType("(Array,Foo)", getType("c"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAllPropertyReference
  public void testAllPropertyReference() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.prop = function() { this.prop2(); }\n"
             + "Foo.prototype.prop2 = function() { b = new Foo; }\n"
             + "var a = new Foo;\n"
             + "a = [][0];\n"
             + "function fun(a) {\n"
             + "  return a.prop();\n"
             + "}\n"
             + "var b;\n"
             + "fun(a);\n"
             );

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallFunction
  public void testCallFunction() {
    testSame(" function Foo() { this.a = new A; }\n"
             + " function Bar() {\n"
             + "  Foo.call(this);\n"
             + "}\n"
             + " function A() {};\n"
             + "new Bar;");

    assertTrue(isCalled(getType("Foo")));
    assertTrue(isCalled(getType("A")));
    ConcreteType fooType = getThisType(getType("Foo"));
    assertType("A", getPropertyType(fooType, "a"));

    ConcreteType barType = getThisType(getType("Bar"));
    assertType("A", getPropertyType(barType, "a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallFunctionWithArgs
  public void testCallFunctionWithArgs() {
    testSame(" function Foo(o) { this.a = o; }\n"
             + " function Bar() {\n"
             + "  Foo.call(this, new A());\n"
             + "}\n"
             + " function A() {};\n"
             + "var b = new Bar;");

    assertTrue(isCalled(getType("Foo")));
    assertTrue(isCalled(getType("A")));

    ConcreteType barType = getThisType(getType("Bar"));
    assertType("A", getPropertyType(barType, "a"));

    ConcreteType fooType = getThisType(getType("Foo"));
    assertType("A", getPropertyType(fooType, "a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallPrototypeFunction
  public void testCallPrototypeFunction() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.a = function() { return new A; }\n"
             + "Foo.prototype.a = function() { return new A; };\n"
             + " function Bar() {}\n"
             + ""
             + "Bar.prototype.a = function() { return new B; };\n"
             + " function A() {};\n"
             + " function B() {};\n"
             + "var ret = Foo.prototype.a.call(new Bar);");

    assertType("A", getType("ret"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallPrototypeFunctionWithArgs
  public void testCallPrototypeFunctionWithArgs() {
    testSame(" function Foo() { this.p = null }\n"
             + "Foo.prototype.set = function(arg) { this.p = arg; };\n"
             + "Foo.prototype.get = function() { return this.p; };\n"
             + " function A() {};\n"
             + "Foo.prototype.set.call(new Foo, new A);\n"
             + "var ret = Foo.prototype.get.call(new Foo);");

    ConcreteType fooP = getFunctionPrototype(getType("Foo"));
    ConcreteFunctionType gFun = getPropertyType(fooP, "get").toFunction();
    ConcreteFunctionType sFun = getPropertyType(fooP, "set").toFunction();

    assertTrue(isCalled(sFun));
    assertTrue(isCalled(gFun));
    assertTrue(isCalled(getType("A")));
    assertType("A", getType("ret"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSetTimeout
  public void testSetTimeout() {
    testSame(" function Window() {};\n"
             + "Window.prototype.setTimeout = function(f, t) {};\n"
             + " var window;",
             " function A() {}\n"
             + "A.prototype.handle = function() { foo(); };\n"
             + "function foo() {}\n"
             + "window.setTimeout((new A).handle, 3);", null);

    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternType
  public void testExternType() {
    testSame(" function T() {};\n"
             + " function Ext() {};\n"
             + "\n"
             + "Ext.prototype.getT = function() {};\n"
             + " Ext.prototype.prop;\n"
             + " var ext;",
             "var b = ext.getT();\n"
             + "var p = ext.prop;", null);

    assertType("Ext", getType("ext"));
    assertType("T", getType("b"));
    assertType("T", getType("p"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternSubTypes
  public void testExternSubTypes() {
    testSame(" function A() {};\n"
             + " function B() {};\n"
             + " function C() {};\n"
             + " function D() {};\n"
             + " function Ext() {};\n"
             + " Ext.prototype.a;\n"
             + " Ext.prototype.b;\n"
             + " Ext.prototype.d;\n"
             + " Ext.prototype.getA = function() {};\n"
             + " Ext.prototype.getB = function() {};\n",
             "var a = (new Ext).a;\n"
             + "var a2 = (new Ext).getA();\n"
             + "var b = (new Ext).b;\n"
             + "var b2 = (new Ext).getB();\n"
             + "var d = (new Ext).d;\n", null);

    assertType("(A,B,C,D)", getType("a"));
    assertType("(A,B,C,D)", getType("a2"));
    assertType("(B,D)", getType("b"));
    assertType("(B,D)", getType("b2"));
    assertType("D", getType("d"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternSubTypesForObject
  public void testExternSubTypesForObject() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES
             + " function A() {};\n"
             + " function B() {};\n"
             + " "
             + "Object.prototype.eval = function(code) {};\n"
             + "\n"
             + "A.prototype.a;\n"
             + "\n"
             + "A.prototype.b = function(){};\n",
             "var a = (new A).b()", null, null);
    assertType("(A,ActiveXObject,Array,B,Boolean,Date,Error,EvalError,"
               + "Function,Number,Object,"
               + "RangeError,ReferenceError,RegExp,String,SyntaxError,"
               + "TypeError,URIError)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitPropCall
  public void testImplicitPropCall() {
    testSame(" function Window() {};\n"
             + "\n"
             + "Window.prototype.setTimeout = function(f, d) {};",
             "function foo() {};\n"
             + "(new Window).setTimeout(foo, 20);", null);

    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitPropCallWithArgs
  public void testImplicitPropCallWithArgs() {
    testSame(" function Window() {};\n"
             + " function EventListener() {};\n"
             + "\n"
             + "Window.prototype.addEventListener = function(t, f) {};\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Window).addEventListener('click', foo);", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testUntypedImplicitCallFromProperty
  public void testUntypedImplicitCallFromProperty() {
    testSame(" function Element() {};\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};"
             + " Event.prototype.erv;",
             " function foo(evt) { return bar(evt); };\n"
             + "function bar(a) { return a.type() }\n"
             + " var ar = new Element;\n"
             + "ar.onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertTrue(isCalled(getType("bar")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Event", getParamType(getType("bar"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromProperty
  public void testImplicitCallFromProperty() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Element).onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromPropertyOfUnion
  public void testImplicitCallFromPropertyOfUnion() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Element).onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromPropertyOfAllType
  public void testImplicitCallFromPropertyOfAllType() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "var elems = [];\n"
             + "var elem = elems[0];\n" 
             + "elem.onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToCast
  public void testRestrictToCast() {
    testSame(" function Foo() {};\n"
             + "var a = [];\n"
             + "var foo =  (a[0]);\n"
             + "var u = a[0];\n"
             + "new Foo");

    assertType("Foo", getType("foo"));
    assertType("(Array,Foo)", getType("u"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToInterfaceCast
  public void testRestrictToInterfaceCast() {
    testSame(" function Foo() {};\n"
             + " function Int() {};\n"
             + "var a = [];\n"
             + "var foo =  (a[0]);\n"
             + "new Foo");

    assertType("Foo", getType("foo"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToCastWithNonInstantiatedTypes
  public void testRestrictToCastWithNonInstantiatedTypes() {
    testSame(
             " function Super() {}\n"
             + " function Foo() {};\n"
             + "Foo.prototype.blah = function() { foofunc() };\n"
             + " function Bar() {};\n"
             + "Bar.prototype.blah = function() { barfunc() };\n"
             + "function barfunc() {}\n"
             + "function foofunc() {}\n"
             + "var a = [];\n"
             + "var u =  (a[0]);\n"
             + "u.blah()\n"
             + "new Foo");

    assertTrue(isCalled(getType("foofunc")));
    assertFalse(isCalled(getType("barfunc")));
    assertType("Array", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testFunctionToString
  public void testFunctionToString() {
    testSame(" function Foo() {}\n"
             + "\n"
             + "function Bar() { Foo.call(this); }\n"
             + "var a = function(a) { return new Foo; };\n;"
             + "a(new Foo);\n"
             + "a(new Bar);\n"
             + "new Bar;");

    assertType("function ((Bar,Foo)): Foo", getType("a"));
    assertType("function (this:(Bar,Foo)): ()", getType("Foo"));
    assertType("function (this:Bar): ()", getType("Bar"));
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testDefine
  public void testDefine() {
    test("define(['foo', 'bar'], function(foo, bar) { foo(bar); bar+1; })",
        "var foo=require('foo'); var bar=require('bar');foo(bar);bar+1");
    test("define(['foo', 'bar'], function(foo, bar, baz) { foo(bar); bar+1; })",
        "var foo=require('foo'); var bar=require('bar');" +
        "var baz = null;foo(bar);bar+1");
    test("define(['foo', 'bar'], function(foo, bar) { return { test: 1 } })",
        "var foo=require('foo'); var bar=require('bar');" +
        "module.exports={test:1}");
    test("define(['foo', 'bar'], function(foo, bar, exports) { " +
        "return { test: 1 } })",
        "var foo=require('foo'); var bar=require('bar');" +
        "module.exports={test:1}");
    test("define(['foo', 'bar'], function(foo, bar, exports, module) { " +
        "return { test: 1 } })",
        "var foo=require('foo'); var bar=require('bar');" +
        "module.exports={test:1}");
    test("define(['foo', 'bar'], function(foo, bar, exports, module, baz) { " +
        "return { test: 1 } })",
        "var foo=require('foo'); var bar=require('bar');var baz = null;" +
        "module.exports={test:1}");
    test("define(['foo', 'bar'], function(foo) { return { test: 1 } })",
        "var foo=require('foo'); require('bar'); module.exports={test:1}");
    test("define(['foo', 'bar'], function(test) { return { test: 1 } })",
        "var test=require('foo'); require('bar'); module.exports={test:1}");
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testVarRenaming
  public void testVarRenaming() {
    final String suffix = TransformAMDToCJSModule.VAR_RENAME_SUFFIX;
    test("var foo; define(['foo', 'bar'], function(foo, bar) { " +
        "foo(bar); bar+1; })",
        "var foo; var foo" + suffix +"0=require('foo');" +
        "var bar=require('bar');foo" + suffix +"0(bar);bar+1");
    test("function foo() {}; define(['foo', 'bar'], " +
        "function(foo, bar) { foo(bar); bar+1; })",
        "function foo() {}; var foo" + suffix +"0=require('foo'); " +
        "var bar=require('bar');foo" + suffix +"0(bar);bar+1");
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testDefineOnlyFunction
  public void testDefineOnlyFunction() {
    test("define(function() { return { test: 1 } })",
        "module.exports={test:1}");
    test("define(function(exports, module) { return { test: 1 } })",
        "module.exports={test:1}");
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testObjectLit
  public void testObjectLit() {
    test("define({foo: 'bar'})", "exports={foo: 'bar'}");
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testPlugins
  public void testPlugins() {
    test("define(['foo', 'text!foo'], function(foo, text) {})",
        "var foo = require('foo'); var text = null;",
        null, TransformAMDToCJSModule.REQUIREJS_PLUGINS_NOT_SUPPORTED_WARNING);
    test("define(['foo', 'text!foo?bar'], function(foo, bar) {})",
        "var foo = require('foo'); var bar = require('bar'); ",
        null, TransformAMDToCJSModule.REQUIREJS_PLUGINS_NOT_SUPPORTED_WARNING);
    test("define(['foo', 'text!foo?:bar'], function(foo, bar) {})",
        "var foo = require('foo'); var bar = null;",
        null, TransformAMDToCJSModule.REQUIREJS_PLUGINS_NOT_SUPPORTED_WARNING);
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testUnsupportedForms
  public void testUnsupportedForms() {
    testUnsupported("define()");
    testUnsupported("define([], function() {}, 1)");
    testUnsupported("define({}, function() {})");
    testUnsupported("define('test', function() {})");
    testUnsupported("define([])");
    testUnsupported("define(true)");
    testUnsupported("define(1)");
    testNonTopLevelDefine("var x = define(function() {});");
    testNonTopLevelDefine("if(define(function() {})) {}");
  }

// com.google.javascript.jscomp.TransformAMDToCJSModuleTest::testLocalDefine
  public void testLocalDefine() {
    testSame("(function() { function define() {}; define({}); })()");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionAritySimple
  public void testFunctionAritySimple() {
    assertOk("", "");
    assertOk("a", "'a'");
    assertOk("a,b", "10, 20");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionArityWithOptionalArgs
  public void testFunctionArityWithOptionalArgs() {
    assertOk("a,b,opt_c", "1,2");
    assertOk("a,b,opt_c", "1,2,3");
    assertOk("a,opt_b,opt_c", "1");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionArityWithVarArgs
  public void testFunctionArityWithVarArgs() {
    assertOk("var_args", "");
    assertOk("var_args", "1,2");
    assertOk("a,b,var_args", "1,2");
    assertOk("a,b,var_args", "1,2,3");
    assertOk("a,b,var_args", "1,2,3,4,5");
    assertOk("a,opt_b,var_args", "1");
    assertOk("a,opt_b,var_args", "1,2");
    assertOk("a,opt_b,var_args", "1,2,3");
    assertOk("a,opt_b,var_args", "1,2,3,4,5");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testWrongNumberOfArgs
  public void testWrongNumberOfArgs() {
    assertWarning("a,b,opt_c", "1",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,opt_c", "1,2,3,4",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,c,d", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,var_args", "1",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,opt_c,var_args", "1",
        WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testVarArgsLast
  public void testVarArgsLast() {
    assertWarning("a,b,var_args,c", "1,2,3,4",
        VAR_ARGS_MUST_BE_LAST);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testOptArgsLast
  public void testOptArgsLast() {
    assertWarning("a,b,opt_d,c", "1, 2, 3",
        OPTIONAL_ARG_AT_END);
    assertWarning("a,b,opt_d,c", "1, 2",
        OPTIONAL_ARG_AT_END);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc1
  public void testFunctionsWithJsDoc1() {
    testSame(" function foo(a,b,c) {} foo(1,2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc2
  public void testFunctionsWithJsDoc2() {
    testSame(" function foo(a,b,c) {} foo(1,2,3);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc3
  public void testFunctionsWithJsDoc3() {
    testSame(" " +
             "function foo(a,b,c) {} foo(1);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc4
  public void testFunctionsWithJsDoc4() {
    testSame(" var foo = function(a) {}; foo();");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc5
  public void testFunctionsWithJsDoc5() {
    testSame(" var foo = function(a) {}; foo(1,2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc6
  public void testFunctionsWithJsDoc6() {
    testSame(" var foo = function(a, b) {}; foo();",
             WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc7
  public void testFunctionsWithJsDoc7() {
    String fooDfn = " var foo = function(b) {};";
    testSame(fooDfn + "foo();");
    testSame(fooDfn + "foo(1);");
    testSame(fooDfn + "foo(1, 2);", WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionWithDefaultCodingConvention
  public void testFunctionWithDefaultCodingConvention() {
    convention = CodingConventions.getDefault();
    testSame("var foo = function(x) {}; foo(1, 2);", WRONG_ARGUMENT_COUNT);
    testSame("var foo = function(opt_x) {}; foo(1, 2);", WRONG_ARGUMENT_COUNT);
    testSame("var foo = function(var_args) {}; foo(1, 2);",
        WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testMethodCalls
  public void testMethodCalls() {
    final String METHOD_DEFS =
      "\n" +
      "function Foo() {}" +
      
      "function twoArg(arg1, arg2) {};" +
      "Foo.prototype.prototypeMethod = twoArg;" +
      "Foo.staticMethod = twoArg;" +
      
      "\n" +
      "function Bar() {}";

    
    testSame(METHOD_DEFS +
        "var f = new Foo();f.prototypeMethod(1, 2, 3);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
    
    testSame(METHOD_DEFS +
        "var f = new Foo();f.prototypeMethod(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);

    
    testSame(METHOD_DEFS +
        "Foo.staticMethod(1, 2, 3);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
    
    testSame(METHOD_DEFS +
        "Foo.staticMethod(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);

    
    testSame(METHOD_DEFS + "Foo();", TypeCheck.CONSTRUCTOR_NOT_CALLABLE);

    
    
    testSame(METHOD_DEFS + "Bar();", null);

    
    testSame(METHOD_DEFS, "Foo();", TypeCheck.CONSTRUCTOR_NOT_CALLABLE);

    
    
    testSame(METHOD_DEFS, "Bar();", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testInitialTypingScope
  public void testInitialTypingScope() {
    Scope s = new TypedScopeCreator(compiler,
        CodingConventions.getDefault()).createInitialScope(
            new Node(Token.BLOCK));

    assertTypeEquals(ARRAY_FUNCTION_TYPE, s.getVar("Array").getType());
    assertTypeEquals(BOOLEAN_OBJECT_FUNCTION_TYPE,
        s.getVar("Boolean").getType());
    assertTypeEquals(DATE_FUNCTION_TYPE, s.getVar("Date").getType());
    assertTypeEquals(ERROR_FUNCTION_TYPE, s.getVar("Error").getType());
    assertTypeEquals(EVAL_ERROR_FUNCTION_TYPE,
        s.getVar("EvalError").getType());
    assertTypeEquals(NUMBER_OBJECT_FUNCTION_TYPE,
        s.getVar("Number").getType());
    assertTypeEquals(OBJECT_FUNCTION_TYPE, s.getVar("Object").getType());
    assertTypeEquals(RANGE_ERROR_FUNCTION_TYPE,
        s.getVar("RangeError").getType());
    assertTypeEquals(REFERENCE_ERROR_FUNCTION_TYPE,
        s.getVar("ReferenceError").getType());
    assertTypeEquals(REGEXP_FUNCTION_TYPE, s.getVar("RegExp").getType());
    assertTypeEquals(STRING_OBJECT_FUNCTION_TYPE,
        s.getVar("String").getType());
    assertTypeEquals(SYNTAX_ERROR_FUNCTION_TYPE,
        s.getVar("SyntaxError").getType());
    assertTypeEquals(TYPE_ERROR_FUNCTION_TYPE,
        s.getVar("TypeError").getType());
    assertTypeEquals(URI_ERROR_FUNCTION_TYPE,
        s.getVar("URIError").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrivateType
  public void testPrivateType() throws Exception {
    testTypes(
        " var x = false;",
        "initializing variable\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck1
  public void testTypeCheck1() throws Exception {
    testTypes("function foo(){ if (foo()) return; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck2
  public void testTypeCheck2() throws Exception {
    testTypes("function foo(){ var x=foo(); x--; }",
        "increment/decrement\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck4
  public void testTypeCheck4() throws Exception {
    testTypes("function foo(){ !foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck5
  public void testTypeCheck5() throws Exception {
    testTypes("function foo(){ var a = +foo(); }",
        "sign operator\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck6
  public void testTypeCheck6() throws Exception {
    testTypes(
        "function foo(){" +
        "var a;if (a == foo())return;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck8
  public void testTypeCheck8() throws Exception {
    testTypes("function foo(){do {} while (foo());}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck9
  public void testTypeCheck9() throws Exception {
    testTypes("function foo(){while (foo());}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck10
  public void testTypeCheck10() throws Exception {
    testTypes("function foo(){for (;foo(););}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck11
  public void testTypeCheck11() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a = b;",
        "assignment\n" +
        "found   : String\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck12
  public void testTypeCheck12() throws Exception {
    testTypes("function foo(){var a = 3^foo();}",
        "bad right operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck13
  public void testTypeCheck13() throws Exception {
    testTypes("var i; i=/xx/;",
        "assignment\n" +
        "found   : RegExp\n" +
        "required: (Number|String)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck14
  public void testTypeCheck14() throws Exception {
    testTypes("function foo(opt_a){}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck15
  public void testTypeCheck15() throws Exception {
    testTypes("var x;x=null;x=10;",
        "assignment\n" +
        "found   : number\n" +
        "required: (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck16
  public void testTypeCheck16() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck17
  public void testTypeCheck17() throws Exception {
    testTypes("\n" +
        "function a(opt_foo){\nreturn (opt_foo);\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck18
  public void testTypeCheck18() throws Exception {
    testTypes("\n function a(){return new RegExp();}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck19
  public void testTypeCheck19() throws Exception {
    testTypes("\n function a(){return new Array();}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck20
  public void testTypeCheck20() throws Exception {
    testTypes("\n function a(){return new Date();}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckBasicDowncast
  public void testTypeCheckBasicDowncast() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckNoDowncastToNumber
  public void testTypeCheckNoDowncastToNumber() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n",
        "initializing variable\n" +
        "found   : foo\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck21
  public void testTypeCheck21() throws Exception {
    testTypes("var foo;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck22
  public void testTypeCheck22() throws Exception {
    testTypes("\nfunction foo(p){}\n" +
                  "function Element(){}\n" +
                  "var v;\n" +
                  "foo(v);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck23
  public void testTypeCheck23() throws Exception {
    testTypes("var foo; foo = null;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck24
  public void testTypeCheck24() throws Exception {
    testTypes("function MyType(){}\n" +
        "var foo; foo = null;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck25
  public void testTypeCheck25() throws Exception {
    testTypes("function foo( obj) {};"
        + "foo({b: 'abc'});",
        "actual parameter 1 of foo does not match formal parameter\n" +
            "found   : {a: (number|undefined), b: string}\n" +
            "required: {a: number}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck26
  public void testTypeCheck26() throws Exception {
    testTypes("function foo( obj) {};"
        + "foo({a: 'abc'});",
        "actual parameter 1 of foo does not match formal parameter\n"
        + "found   : {a: (number|string)}\n"
        + "required: {a: number}");

  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck27
  public void testTypeCheck27() throws Exception {
    testTypes("function foo( obj) {};"
        + "foo({a: 123});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck28
  public void testTypeCheck28() throws Exception {
    testTypes("function foo( obj) {};"
        + "foo({a: 123});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckInlineReturns
  public void testTypeCheckInlineReturns() throws Exception {
    testTypes(
        "function  foo(x) { return x; }" +
        "var  a = foo('abc');",
        "initializing variable\n"
        + "found   : string\n"
        + "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckDefaultExterns
  public void testTypeCheckDefaultExterns() throws Exception {
    testTypes(" function f(x) {}" +
        "f([].length);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckCustomExterns
  public void testTypeCheckCustomExterns() throws Exception {
    testTypes(
        DEFAULT_EXTERNS + " Array.prototype.oogabooga;",
        " function f(x) {}" +
        "f([].oogabooga);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string", false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheckCustomExterns2
  public void testTypeCheckCustomExterns2() throws Exception {
    testTypes(
        DEFAULT_EXTERNS + " var Enum = {FOO: 1, BAR: 1};",
        " function f(x) {} f(Enum.FOO); f(true);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: Enum.<string>",
        false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedArray1
  public void testTemplatizedArray1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedArray2
  public void testTemplatizedArray2() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : Array.<number>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedArray3
  public void testTemplatizedArray3() throws Exception {
    testTypes(" var f = function(a) { a[1] = 0; return a[0]; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedArray4
  public void testTemplatizedArray4() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedArray5
  public void testTemplatizedArray5() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedArray6
  public void testTemplatizedArray6() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : *\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedArray7
  public void testTemplatizedArray7() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedObject1
  public void testTemplatizedObject1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedObject2
  public void testTemplatizedObject2() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedObject3
  public void testTemplatizedObject3() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedObject4
  public void testTemplatizedObject4() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: E.<string>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTemplatizedObject5
  public void testTemplatizedObject5() throws Exception {
    testTypes(" function F() {" +
        "   this.numbers = {};" +
        "}" +
        "(new F()).numbers['ten'] = '10';",
        "restricted index type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnionOfFunctionAndType
  public void testUnionOfFunctionAndType() throws Exception {
    testTypes(" var a;" +
        " var b = null; a = b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalParameterComparedToUndefined
  public void testOptionalParameterComparedToUndefined() throws Exception {
    testTypes("function foo(opt_a)" +
        "{if (opt_a==undefined) var b = 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalAllType
  public void testOptionalAllType() throws Exception {
    testTypes("function f(opt_x) { return opt_x }\n" +
        "var y;\n" +
        "f(y);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalUnknownNamedType
  public void testOptionalUnknownNamedType() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { return opt_x; }\n" +
        "var T = function() {};",
        "inconsistent return type\n" +
        "found   : (T|undefined)\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionParam
  public void testOptionalArgFunctionParam() throws Exception {
    testTypes("" +
        "function f(a) {a()};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionParam2
  public void testOptionalArgFunctionParam2() throws Exception {
    testTypes("" +
        "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionParam3
  public void testOptionalArgFunctionParam3() throws Exception {
    testTypes("" +
        "function f(a) {a(undefined)};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionParam4
  public void testOptionalArgFunctionParam4() throws Exception {
    String expectedWarning = "Function a: called with 2 argument(s). " +
        "Function requires at least 0 argument(s) and no more than 1 " +
        "argument(s).";

    testTypes("function f(a) {a(3,4)};",
              expectedWarning, false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionParamError
  public void testOptionalArgFunctionParamError() throws Exception {
    String expectedWarning =
        "Bad type annotation. variable length argument must be last";
    testTypes("" +
              "function f(a) {};", expectedWarning, false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalNullableArgFunctionParam
  public void testOptionalNullableArgFunctionParam() throws Exception {
    testTypes("" +
              "function f(a) {a()};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalNullableArgFunctionParam2
  public void testOptionalNullableArgFunctionParam2() throws Exception {
    testTypes("" +
              "function f(a) {a(null)};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalNullableArgFunctionParam3
  public void testOptionalNullableArgFunctionParam3() throws Exception {
    testTypes("" +
              "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionReturn
  public void testOptionalArgFunctionReturn() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()()");
  }

// com.google.javascript.jscomp.TypeCheckTest::testOptionalArgFunctionReturn2
  public void testOptionalArgFunctionReturn2() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()({})");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanType
  public void testBooleanType() throws Exception {
    testTypes("var x = 1 < 2;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction1
  public void testBooleanReduction1() throws Exception {
    testTypes("var x; x = null || \"a\";");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction2
  public void testBooleanReduction2() throws Exception {
    
    
    testTypes("" +
        "(function(s) { return ((s == 'a') && s) || 'b'; })");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction3
  public void testBooleanReduction3() throws Exception {
    testTypes("" +
        "(function(s) { return s && null && 3; })");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction4
  public void testBooleanReduction4() throws Exception {
    testTypes("" +
        "(function(x) { return null || x || null ; })");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction5
  public void testBooleanReduction5() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!x || typeof x == 'string') {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction6
  public void testBooleanReduction6() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!(x && typeof x != 'string')) {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanReduction7
   public void testBooleanReduction7() throws Exception {
    testTypes("var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (!x) {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNullAnd
  public void testNullAnd() throws Exception {
    testTypes("var x;\n" +
        "var r = x && x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNullOr
  public void testNullOr() throws Exception {
    testTypes("var x;\n" +
        "var r = x || x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanPreservation1
  public void testBooleanPreservation1() throws Exception {
    testTypes("var x = \"a\";" +
        "x = ((x == \"a\") && x) || x == \"b\";",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanPreservation2
  public void testBooleanPreservation2() throws Exception {
    testTypes("var x = \"a\"; x = (x == \"a\") || x;",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanPreservation3
  public void testBooleanPreservation3() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "condition always evaluates to false\n" +
        "left : Function\n" +
        "right: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBooleanPreservation4
  public void testBooleanPreservation4() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "inconsistent return type\n" +
        "found   : (boolean|null)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction1
  public void testTypeOfReduction1() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'number' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction2
  public void testTypeOfReduction2() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x != 'string' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction3
  public void testTypeOfReduction3() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'object' ? 1 : x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction4
  public void testTypeOfReduction4() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'undefined' ? {} : x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction5
  public void testTypeOfReduction5() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x != 'number' ? x : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction6
  public void testTypeOfReduction6() throws Exception {
    testTypes("\n" +
        "function f(x) {\n" +
        "return typeof x == 'string' && x.length == 3 ? x : 'a';\n" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction7
  public void testTypeOfReduction7() throws Exception {
    testTypes("var f = function(x) { " +
        "return typeof x == 'number' ? x : 'a'; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction8
  public void testTypeOfReduction8() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isString(x) && x.length == 3 ? x : 'a';\n" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction9
  public void testTypeOfReduction9() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isArray(x) ? 'a' : x;\n" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction10
  public void testTypeOfReduction10() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isArray(x) ? x : [];\n" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction11
  public void testTypeOfReduction11() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isObject(x) ? x : [];\n" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction12
  public void testTypeOfReduction12() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x == 'object' ? x : []; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction13
  public void testTypeOfReduction13() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return goog.isObject(x) ? x : []; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction14
  public void testTypeOfReduction14() throws Exception {
    
    testClosureTypes(
        CLOSURE_DEFS +
        "function f(x) { " +
        "  return goog.isString(arguments[0]) ? arguments[0] : 0;" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction15
  public void testTypeOfReduction15() throws Exception {
    
    testClosureTypes(
        CLOSURE_DEFS +
        "function f(x) { " +
        "  return typeof arguments[0] == 'string' ? arguments[0] : 0;" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeOfReduction16
  public void testTypeOfReduction16() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " function I() {}\n" +
        "\n" +
        "function f(x) { " +
        "  if(goog.isObject(x)) {" +
        "    return (x);" +
        "  }" +
        "  return null;" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction1
  public void testQualifiedNameReduction1() throws Exception {
    testTypes("var x = {};  x.a = 'a';\n" +
        " var f = function() {\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction2
  public void testQualifiedNameReduction2() throws Exception {
    testTypes(" var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return this.a ? this.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction3
  public void testQualifiedNameReduction3() throws Exception {
    testTypes(" var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return typeof this.a == 'string' ? this.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction4
  public void testQualifiedNameReduction4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return goog.isString(this.a) ? this.a : 'a'; }", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction5a
  public void testQualifiedNameReduction5a() throws Exception {
    testTypes("var x = { a:'b' };\n" +
        " var f = function() {\n" +
        "return x.a; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction5b
  public void testQualifiedNameReduction5b() throws Exception {
    testTypes(
        "var x = { a:12 };\n" +
        "\n" +
        "var f = function() {\n" +
        "  return x.a;\n" +
        "}",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction5c
  public void testQualifiedNameReduction5c() throws Exception {
    testTypes(
        " var f = function() {\n" +
        "var x = { a:0 };\n" +
        "return (x.a) ? (x.a) : 'a'; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction6
  public void testQualifiedNameReduction6() throws Exception {
    testTypes(
        " var f = function() {\n" +
        "var x = { get a() {return 'a'}};\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction7
  public void testQualifiedNameReduction7() throws Exception {
    testTypes(
        " var f = function() {\n" +
        "var x = { get a() {return 12}};\n" +
        "return x.a; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction7a
  public void testQualifiedNameReduction7a() throws Exception {
    
    testTypes(
        " var f = function() {\n" +
        "var x = {get a() {return 12}};\n" +
        "return x.a; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction8
  public void testQualifiedNameReduction8() throws Exception {
    testTypes(
        " var f = function() {\n" +
        "var x = {get a() {return 'a'}};\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction9
  public void testQualifiedNameReduction9() throws Exception {
    testTypes(
        " var f = function() {\n" +
        "var x = {  set a(b) {}};\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameReduction10
  public void testQualifiedNameReduction10() throws Exception {
    
    
    testTypes(
        " var f = function() {\n" +
        "var x = {  set a(b) {}};\n" +
        "return x.a ? x.a : 'a'; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef1a
  public void testObjLitDef1a() throws Exception {
    testTypes(
        "var x = { a:12 };\n" +
        "x.a = 'a';",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef1b
  public void testObjLitDef1b() throws Exception {
    testTypes(
        "function f(){" +
          "var x = { a:12 };\n" +
          "x.a = 'a';" +
        "};\n" +
        "f();",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef2a
  public void testObjLitDef2a() throws Exception {
    testTypes(
        "var x = { set a(b){} };\n" +
        "x.a = 'a';",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef2b
  public void testObjLitDef2b() throws Exception {
    testTypes(
        "function f(){" +
          "var x = { set a(b){} };\n" +
          "x.a = 'a';" +
        "};\n" +
        "f();",
        "assignment to property a of x\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef3a
  public void testObjLitDef3a() throws Exception {
    testTypes(
        " var y;\n" +
        "var x = { get a(){} };\n" +
        "y = x.a;",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef3b
  public void testObjLitDef3b() throws Exception {
    testTypes(
      " var y;\n" +
        "function f(){" +
          "var x = { get a(){} };\n" +
          "y = x.a;" +
        "};\n" +
        "f();",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef4
  public void testObjLitDef4() throws Exception {
    testTypes(
        "var x = {" +
          " a:12 };\n",
          "assignment to property a of {a: function (): number}\n" +
          "found   : number\n" +
          "required: function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef5
  public void testObjLitDef5() throws Exception {
    testTypes(
        "var x = {};\n" +
        " x.a = 12;\n",
        "assignment to property a of x\n" +
        "found   : number\n" +
        "required: function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef6
  public void testObjLitDef6() throws Exception {
    testTypes("var lit =  { 'x': 1 };",
        "Illegal key, the object literal is a struct");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjLitDef7
  public void testObjLitDef7() throws Exception {
    testTypes("var lit =  { x: 1 };",
        "Illegal key, the object literal is a dict");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceOfReduction1
  public void testInstanceOfReduction1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return x; } else { return new T(); }\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceOfReduction2
  public void testInstanceOfReduction2() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return ''; } else { return x; }\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUndeclaredGlobalProperty1
  public void testUndeclaredGlobalProperty1() throws Exception {
    testTypes(" var x = {}; x.y = null;" +
        "function f(a) { x.y = a; }" +
        " function g(a) { }" +
        "function h() { g(x.y); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUndeclaredGlobalProperty2
  public void testUndeclaredGlobalProperty2() throws Exception {
    testTypes(" var x = {}; x.y = null;" +
        "function f() { x.y = 3; }" +
        " function g(a) { }" +
        "function h() { g(x.y); }",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : (null|number)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLocallyInferredGlobalProperty1
  public void testLocallyInferredGlobalProperty1() throws Exception {
    
    testTypes(
        " function F() {}" +
        " F.prototype.z;" +
        " var x = {};  x.y;" +
        "function f() { x.y.z = 'abc'; }" +
        " function g(x) {}" +
        "function h() { g(x.y.z); }",
        "assignment to property z of F\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInferredPropagation
  public void testPropertyInferredPropagation() throws Exception {
    testTypes("function f() { return {}; }\n" +
         "function g() { var x = f(); if (x.p) x.a = 'a'; else x.a = 'b'; }\n" +
         "function h() { var x = f(); x.a = false; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference1
  public void testPropertyInference1() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference2
  public void testPropertyInference2() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = null; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference3
  public void testPropertyInference3() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : (boolean|number)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference4
  public void testPropertyInference4() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.x_ = 3;" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference5
  public void testPropertyInference5() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference6
  public void testPropertyInference6() throws Exception {
    testTypes(
        " function F() { }" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference7
  public void testPropertyInference7() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference8
  public void testPropertyInference8() throws Exception {
    testTypes(
        " function F() { " +
        "   this.x_ = 'x';" +
        "}" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "assignment to property x_ of F\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference9
  public void testPropertyInference9() throws Exception {
    testTypes(
        " function A() {}" +
        " function f() { " +
        "  return function() {};" +
        "}" +
        "var g = f();" +
        " g.prototype.bar_ = null;",
        "assignment\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyInference10
  public void testPropertyInference10() throws Exception {
    
    
    
    
    testTypes(
        " function A() {}" +
        " function f() { " +
        "  return function() {};" +
        "}" +
        "var g = f();" +
        " g.prototype.bar_ = 1;" +
        "var h = f();" +
        " h.prototype.bar_ = 1;",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoPersistentTypeInferenceForObjectProperties
  public void testNoPersistentTypeInferenceForObjectProperties()
      throws Exception {
    testTypes("\n" +
        "function s1(o,x) { o.x = x; }\n" +
        "\n" +
        "function g1(o) { return typeof o.x == 'undefined' ? '' : o.x; }\n" +
        "\n" +
        "function s2(o,x) { o.x = x; }\n" +
        "\n" +
        "function g2(o) { return typeof o.x == 'undefined' ? 0 : o.x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNoPersistentTypeInferenceForFunctionProperties
  public void testNoPersistentTypeInferenceForFunctionProperties()
      throws Exception {
    testTypes("\n" +
        "function s1(o,x) { o.x = x; }\n" +
        "\n" +
        "function g1(o) { return typeof o.x == 'undefined' ? '' : o.x; }\n" +
        "\n" +
        "function s2(o,x) { o.x = x; }\n" +
        "\n" +
        "function g2(o) { return typeof o.x == 'undefined' ? 0 : o.x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectPropertyTypeInferredInLocalScope1
  public void testObjectPropertyTypeInferredInLocalScope1() throws Exception {
    testTypes("\n" +
        "function f(o) { o.x = 1; return o.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectPropertyTypeInferredInLocalScope2
  public void testObjectPropertyTypeInferredInLocalScope2() throws Exception {
    testTypes("" +
        "function f(o, x) { o.x = 'a';\nif (x) {o.x = x;}\nreturn o.x; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectPropertyTypeInferredInLocalScope3
  public void testObjectPropertyTypeInferredInLocalScope3() throws Exception {
    testTypes("" +
        "function f(o, x) { if (x) {o.x = x;} else {o.x = 'a';}\nreturn o.x; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty1
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty1()
      throws Exception {
    testTypes("var T = function() { this.x = ''; };\n" +
        " T.prototype.x = 0;",
        "assignment to property x of T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty2
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty2()
      throws Exception {
    testTypes("var T = function() { this.x = ''; };\n" +
        " T.prototype.x;",
        "assignment to property x of T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty3
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty3()
      throws Exception {
    testTypes(" var n = {};\n" +
        " n.T = function() { this.x = ''; };\n" +
        " n.T.prototype.x = 0;",
        "assignment to property x of n.T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty4
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty4()
      throws Exception {
    testTypes("var n = {};\n" +
        " n.T = function() { this.x = ''; };\n" +
        " n.T.prototype.x = 0;",
        "assignment to property x of n.T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyUsedBeforeDefinition1
  public void testPropertyUsedBeforeDefinition1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "" +
        "T.prototype.f = function() { return this.g(); };\n" +
        " T.prototype.g = function() { return 1; };\n",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyUsedBeforeDefinition2
  public void testPropertyUsedBeforeDefinition2() throws Exception {
    testTypes("var n = {};\n" +
        " n.T = function() {};\n" +
        "" +
        "n.T.prototype.f = function() { return this.g(); };\n" +
        " n.T.prototype.g = function() { return 1; };\n",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd1
  public void testAdd1() throws Exception {
    testTypes("function foo(){var a = 'abc'+foo();}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd2
  public void testAdd2() throws Exception {
    testTypes("function foo(){var a = foo()+4;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd3
  public void testAdd3() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 'b';" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd4
  public void testAdd4() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 'b';" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd5
  public void testAdd5() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd6
  public void testAdd6() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 5;" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd7
  public void testAdd7() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 'b';" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd8
  public void testAdd8() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd9
  public void testAdd9() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd10
  public void testAdd10() throws Exception {
    
    testTypes(
        suppressMissingProperty("e", "f") +
        " var a = 5;" +
        " var c = a + d.e.f;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd11
  public void testAdd11() throws Exception {
    
    testTypes(
        suppressMissingProperty("e", "f") +
        " var a = 5;" +
        " var c = a + d.e.f;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd12
  public void testAdd12() throws Exception {
    testTypes(" function a() { return 5; }" +
        " var b = 5;" +
        " var c = a() + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd13
  public void testAdd13() throws Exception {
    testTypes(" var a = 5;" +
        " function b() { return 5; }" +
        " var c = a + b();",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd14
  public void testAdd14() throws Exception {
    testTypes(" var a = null;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd15
  public void testAdd15() throws Exception {
    testTypes(" var a = 5;" +
        " function b() { return 5; }" +
        " var c = a + b();",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd16
  public void testAdd16() throws Exception {
    testTypes(" var a = undefined;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd17
  public void testAdd17() throws Exception {
    testTypes(" var a = 5;" +
        " var b = undefined;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd18
  public void testAdd18() throws Exception {
    testTypes("function f() {};" +
        " var a = 'a';" +
        " var c = a + f();",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAdd19
  public void testAdd19() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }
