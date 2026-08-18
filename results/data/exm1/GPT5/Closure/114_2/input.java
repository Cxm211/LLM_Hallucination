// buggy code
    private void recordAssignment(NodeTraversal t, Node n, Node recordNode) {
      Node nameNode = n.getFirstChild();
      Node parent = n.getParent();
      NameInformation ns = createNameInformation(t, nameNode);
      if (ns != null) {
        if (parent.isFor() && !NodeUtil.isForIn(parent)) {
          // Patch for assignments that appear in the init,
          // condition or iteration part of a FOR loop.  Without
          // this change, all 3 of those parts try to claim the for
          // loop as their dependency scope.  The last assignment in
          // those three fields wins, which can result in incorrect
          // reference edges between referenced and assigned variables.
          //
          // TODO(user) revisit the dependency scope calculation
          // logic.
          if (parent.getFirstChild().getNext() != n) {
            recordDepScope(recordNode, ns);
          } else {
            recordDepScope(nameNode, ns);
          }
        } else {
          // The rhs of the assignment is the caller, so it's used by the
          // context. Don't associate it w/ the lhs.
          // FYI: this fixes only the specific case where the assignment is the
          // caller expression, but it could be nested deeper in the caller and
          // we would still get a bug.
          // See testAssignWithCall2 for an example of this.
          recordDepScope(recordNode, ns);
        }
      }
    }

// relevant test
// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias5a
  public void testNoRemoveWindowPropertyAlias5a() {
    
    test(
        "var self_; self_ = window || {};\n" +
        "self_['qs'] = function() {};",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias6
  public void testNoRemoveWindowPropertyAlias6() {
    testSame(
        "var self_ = (window.gbar = window.gbar || {});\n" +
        "self_.qs = function() {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias6a
  public void testNoRemoveWindowPropertyAlias6a() {
    testSame(
        "var self_; self_ = (window.gbar = window.gbar || {});\n" +
        "self_.qs = function() {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias7
  public void testNoRemoveWindowPropertyAlias7() {
    testSame(
        "var self_ = (window = window || {});\n" +
        "self_['qs'] = function() {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveWindowPropertyAlias7a
  public void testNoRemoveWindowPropertyAlias7a() {
    testSame(
        "var self_; self_ = (window = window || {});\n" +
        "self_['qs'] = function() {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias0
  public void testNoRemoveAlias0() {
    testSame(
        "var x = {}; function f() { return x; }; " +
        "f().style.display = 'block';" +
        "alert(x.style)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias1
  public void testNoRemoveAlias1() {
    testSame(
        "var x = {}; function f() { return x; };" +
        "var map = f();\n" +
        "map.style.display = 'block';" +
        "alert(x.style)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias2
  public void testNoRemoveAlias2() {
    testSame(
        "var x = {};" +
        "var map = (function () { return x; })();\n" +
        "map.style = 'block';" +
        "alert(x.style)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAlias3
  public void testNoRemoveAlias3() {
    testSame(
        "var x = {}; function f() { return x; };" +
        "var map = {}\n" +
        "map[1] = f();\n" +
        "map[1].style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAliasOfExternal0
  public void testNoRemoveAliasOfExternal0() {
    testSame(
        "document.getElementById('foo').style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAliasOfExternal1
  public void testNoRemoveAliasOfExternal1() {
    testSame(
        "var map = document.getElementById('foo');\n" +
        "map.style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveAliasOfExternal2
  public void testNoRemoveAliasOfExternal2() {
    testSame(
        "var map = {}\n" +
        "map[1] = document.getElementById('foo');\n" +
        "map[1].style.display = 'block';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveThrowReference1
  public void testNoRemoveThrowReference1() {
    testSame(
      "var e = {}\n" +
      "throw e;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoRemoveThrowReference2
  public void testNoRemoveThrowReference2() {
    testSame(
      "function e() {}\n" +
      "throw new e();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit1
  public void testClassDefinedInObjectLit1() {
    test(
      "var data = {Foo: function() {}};" +
      "data.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit2
  public void testClassDefinedInObjectLit2() {
    test(
      "var data = {}; data.bar = {Foo: function() {}};" +
      "data.bar.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit3
  public void testClassDefinedInObjectLit3() {
    test(
      "var data = {bar: {Foo: function() {}}};" +
      "data.bar.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testClassDefinedInObjectLit4
  public void testClassDefinedInObjectLit4() {
    test(
      "var data = {};" +
      "data.baz = {bar: {Foo: function() {}}};" +
      "data.baz.bar.Foo.prototype.toString = function() {};",
      "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testVarReferencedInClassDefinedInObjectLit1
  public void testVarReferencedInClassDefinedInObjectLit1() {
    testSame(
      "var ref = 3;" +
      "var data = {Foo: function() { this.x = ref; }};" +
      "window.Foo = data.Foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testVarReferencedInClassDefinedInObjectLit2
  public void testVarReferencedInClassDefinedInObjectLit2() {
    testSame(
      "var ref = 3;" +
      "var data = {Foo: function() { this.x = ref; }," +
      "            Bar: function() {}};" +
      "window.Bar = data.Bar;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testArrayExt
  public void testArrayExt() {
    testSame(
      "Array.prototype.foo = function() { return 1 };" +
      "var y = [];" +
      "switch (y.foo()) {" +
      "}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testArrayAliasExt
  public void testArrayAliasExt() {
    testSame(
      "Array$X = Array;" +
      "Array$X.prototype.foo = function() { return 1 };" +
      "function Array$X() {}" +
      "var y = [];" +
      "switch (y.foo()) {" +
      "}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternalAliasInstanceof1
  public void testExternalAliasInstanceof1() {
    test(
      "Array$X = Array;" +
      "function Array$X() {}" +
      "var y = [];" +
      "if (y instanceof Array) {}",
      "var y = [];" +
      "if (y instanceof Array) {}"
      );
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternalAliasInstanceof2
  public void testExternalAliasInstanceof2() {
    testSame(
      "Array$X = Array;" +
      "function Array$X() {}" +
      "var y = [];" +
      "if (y instanceof Array$X) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternalAliasInstanceof3
  public void testExternalAliasInstanceof3() {
    testSame(
      "var b = Array;" +
      "var y = [];" +
      "if (y instanceof b) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAliasInstanceof4
  public void testAliasInstanceof4() {
    testSame(
      "function Foo() {};" +
      "var b = Foo;" +
      "var y = new Foo();" +
      "if (y instanceof b) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAliasInstanceof5
  public void testAliasInstanceof5() {
    
    test(
      "function Foo() {}" +
      "function Bar() {}" +
      "var b = x ? Foo : Bar;" +
      "var y = new Foo();" +
      "if (y instanceof b) {}",
      "function Foo() {}" +
      "var y = new Foo;" +
      "if (false){}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testBrokenNamespaceWithPrototypeAssignment
  public void testBrokenNamespaceWithPrototypeAssignment() {
    test("var x = {}; x.a.prototype = 1", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemovePrototypeAliases
  public void testRemovePrototypeAliases() {
    test(
        "function g() {}" +
        "function F() {} F.prototype.bar = g;" +
        "window.g = g;",
        "function g() {}" +
        "window.g = g;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIssue284
  public void testIssue284() {
    test(
        "var goog = {};" +
        "goog.inherits = function(x, y) {};" +
        "var ns = {};" +
        "" +
        "ns.PageSelectionModel = function() {};" +
        "" +
        "ns.PageSelectionModel.FooEvent = function() {};" +
        "" +
        "ns.PageSelectionModel.SelectEvent = function() {};" +
        "goog.inherits(ns.PageSelectionModel.ChangeEvent," +
        "    ns.PageSelectionModel.FooEvent);",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIssue838a
  public void testIssue838a() {
    testSame("var z = window['z'] || (window['z'] = {});\n" +
         "z['hello'] = 'Hello';\n" +
         "z['world'] = 'World';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIssue838b
  public void testIssue838b() {
    testSame(
         "var z;" +
         "window['z'] = z || (z = {});\n" +
         "z['hello'] = 'Hello';\n" +
         "z['world'] = 'World';");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIssue874a
  public void testIssue874a() {
    testSame(
        "var a = a || {};\n" +
        "var b = a;\n" +
        "b.View = b.View || {}\n" +
        "var c = b.View;\n" +
        "c.Editor = function f(d, e) {\n" +
        "  return d + e\n" +
        "};\n" +
        "window.ImageEditor.View.Editor = a.View.Editor;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIssue874b
  public void testIssue874b() {
    testSame(
        "var b;\n" +
        "var c = b = {};\n" +
        "c.Editor = function f(d, e) {\n" +
        "  return d + e\n" +
        "};\n" +
        "window['Editor'] = b.Editor;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIssue874c
  public void testIssue874c() {
    testSame(
        "var b, c;\n" +
        "c = b = {};\n" +
        "c.Editor = function f(d, e) {\n" +
        "  return d + e\n" +
        "};\n" +
        "window['Editor'] = b.Editor;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIssue874d
  public void testIssue874d() {
    testSame(
        "var b = {}, c;\n" +
        "c = b;\n" +
        "c.Editor = function f(d, e) {\n" +
        "  return d + e\n" +
        "};\n" +
        "window['Editor'] = b.Editor;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIssue874e
  public void testIssue874e() {
    testSame(
        "var a;\n" +
        "var b = a || (a = {});\n" +
        "var c = b.View || (b.View = {});\n" +
        "c.Editor = function f(d, e) {\n" +
        "  return d + e\n" +
        "};\n" +
        "window.ImageEditor.View.Editor = a.View.Editor;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testBug6575051
  public void testBug6575051() {
    testSame(
        "var hackhack = window['__o_o_o__'] = window['__o_o_o__'] || {};\n" +
        "window['__o_o_o__']['va'] = 1;\n" +
        "hackhack['Vb'] = 1;");
  }
