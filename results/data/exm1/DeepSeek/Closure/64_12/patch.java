public void toSource(final CodeBuilder cb, final int inputSeqNum, final Node root) {
    runInCompilerThread(new Callable<Void>() {
      public Void call() throws Exception {
        if (options.printInputDelimiter) {
          if ((cb.getLength() > 0) && !cb.endsWith("\n")) {
            cb.append("\n");
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

        if (options.sourceMapOutputPath != null) {
          sourceMap.setStartingPosition(
              cb.getLineIndex(), cb.getColumnIndex());
        }

        String code = toSource(root, sourceMap);
        if (!code.isEmpty()) {
          cb.append(code);

          // In order to avoid parse ambiguity when files are concatenated
          // together, all files should end in a semi-colon.
          // Check the last non-whitespace character to determine if there is
          // already a semicolon (ignoring trailing whitespace/newlines).
          int len = code.length();
          int i = len - 1;
          while (i >= 0 && Character.isWhitespace(code.charAt(i))) {
            i--;
          }
          boolean hasSemiColon = (i >= 0) && (code.charAt(i) == ';');
          if (!hasSemiColon) {
            cb.append(";");
          }
        }
        return null;
      }
    });
  }