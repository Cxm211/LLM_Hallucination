// ===== FIXED com.google.javascript.jscomp.Compiler :: call() [lines 549-552] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-64-fixed/src/com/google/javascript/jscomp/Compiler.java =====
      public Result call() throws Exception {
        compileInternal();
        return getResult();
      }

// ===== FIXED com.google.javascript.jscomp.Compiler :: toSource(CodeBuilder, int, Node) [lines 1393-1452] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-64-fixed/src/com/google/javascript/jscomp/Compiler.java =====
  public void toSource(final CodeBuilder cb,
                       final int inputSeqNum,
                       final Node root) {
    runInCompilerThread(new Callable<Void>() {
      public Void call() throws Exception {
        if (options.printInputDelimiter) {
          if ((cb.getLength() > 0) && !cb.endsWith("\n")) {
            cb.append("\n");  // Make sure that the label starts on a new line
          }
          Preconditions.checkState(root.getType() == Token.SCRIPT);

          String delimiter = options.inputDelimiter;

          String sourceName = (String)root.getProp(Node.SOURCENAME_PROP);
          Preconditions.checkState(sourceName != null);
          Preconditions.checkState(!sourceName.isEmpty());

          delimiter = delimiter.replaceAll("%name%", sourceName)
            .replaceAll("%num%", String.valueOf(inputSeqNum));

          cb.append(delimiter)
            .append("\n");
        }
        if (root.getJSDocInfo() != null &&
            root.getJSDocInfo().getLicense() != null) {
          cb.append("/*\n")
            .append(root.getJSDocInfo().getLicense())
            .append("*/\n");
        }

        // If there is a valid source map, then indicate to it that the current
        // root node's mappings are offset by the given string builder buffer.
        if (options.sourceMapOutputPath != null) {
          sourceMap.setStartingPosition(
              cb.getLineIndex(), cb.getColumnIndex());
        }

        // if LanguageMode is ECMASCRIPT5_STRICT, only print 'use strict'
        // for the first input file
        String code = toSource(root, sourceMap, inputSeqNum == 0);
        if (!code.isEmpty()) {
          cb.append(code);

          // In order to avoid parse ambiguity when files are concatenated
          // together, all files should end in a semi-colon. Do a quick
          // heuristic check if there's an obvious semi-colon already there.
          int length = code.length();
          char lastChar = code.charAt(length - 1);
          char secondLastChar = length >= 2 ?
              code.charAt(length - 2) : '\0';
          boolean hasSemiColon = lastChar == ';' ||
              (lastChar == '\n' && secondLastChar == ';');
          if (!hasSemiColon) {
            cb.append(";");
          }
        }
        return null;
      }
    });
  }

// ===== FIXED com.google.javascript.jscomp.Compiler :: toSource(Node) [lines 1459-1462] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-64-fixed/src/com/google/javascript/jscomp/Compiler.java =====
  String toSource(Node n) {
    initCompilerOptionsIfTesting();
    return toSource(n, null, true);
  }

// ===== FIXED com.google.javascript.jscomp.Compiler :: toSource(Node, SourceMap) [lines 1283-1303] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-64-fixed/src/com/google/javascript/jscomp/Compiler.java =====
  public String toSource() {
    return runInCompilerThread(new Callable<String>() {
      public String call() throws Exception {
        Tracer tracer = newTracer("toSource");
        try {
          CodeBuilder cb = new CodeBuilder();
          if (jsRoot != null) {
            int i = 0;
            for (Node scriptNode = jsRoot.getFirstChild();
                 scriptNode != null;
                 scriptNode = scriptNode.getNext()) {
              toSource(cb, i++, scriptNode);
            }
          }
          return cb.toString();
        } finally {
          stopTracer(tracer, "toSource");
        }
      }
    });
  }
