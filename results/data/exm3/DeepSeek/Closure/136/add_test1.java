// com/google/javascript/jscomp/RenameVarsTest.java
public void testExternVarNotRenamed() {
    // Extern variable should not be renamed
    String externs = "var externVar = 1;";
    test(externs + " alert(externVar);", " alert(externVar);");
  }
