// buggy function
  void add(String newcode) {
    maybeEndStatement();

    if (newcode.length() == 0) {
      return;
    }

    char c = newcode.charAt(0);
    if ((isWordChar(c) || c == '\\') &&
        isWordChar(getLastChar())) {
      // need space to separate. This is not pretty printing.
      // For example: "return foo;"
      append(" ");
      // Do not allow a forward slash to appear after a DIV.
      // For example,
      // REGEXP DIV REGEXP
      // is valid and should print like
      // / // / /
    }

    append(newcode);
  }

// trigger testcase
// com/google/javascript/jscomp/CodePrinterTest.java::testIssue620
public void testIssue620() {
    assertPrint("alert(/ / / / /);", "alert(/ // / /)");
    assertPrint("alert(/ // / /);", "alert(/ // / /)");
  }
