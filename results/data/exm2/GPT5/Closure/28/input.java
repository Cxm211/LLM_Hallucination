    void addIdentifier(String identifier) {
      add(ESTIMATED_IDENTIFIER);
    }

// trigger testcase
public void testCost() {
    checkCost("1", "1");
    checkCost("true", "1");
    checkCost("false", "1");
    checkCost("a", "xx");
    checkCost("a + b", "xx+xx");
    checkCost("foo()", "xx()");
    checkCost("foo(a,b)", "xx(xx,xx)");
    checkCost("10 + foo(a,b)", "0+xx(xx,xx)");
    checkCost("1 + foo(a,b)", "1+xx(xx,xx)");
    checkCost("a ? 1 : 0", "xx?1:0");
    checkCost("a.b", "xx.xx");
    checkCost("new Obj()", "new xx");
    checkCost("function a() {return \"monkey\"}",
              "function xx(){return\"monkey\"}");
  }

public void testIssue728() {
    String f = "var f = function() { return false; };";
    StringBuilder calls = new StringBuilder();
    StringBuilder folded = new StringBuilder();
    for (int i = 0; i < 30; i++) {
      calls.append("if (!f()) alert('x');");
      folded.append("if (!false) alert('x');");
    }

    test(f + calls, folded.toString());
  }
