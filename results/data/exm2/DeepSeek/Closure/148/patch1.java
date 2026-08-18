    void appendTo(Appendable out) throws IOException {
      out.append("[");

      out.append(sourceFile);
      // The source file rarely changes, so cache the escaped string.

      out.append(",");


      out.append(String.valueOf(originalPosition.getLineNumber()));

      out.append(",");
      out.append(String.valueOf(originalPosition.getCharacterIndex()));

      if (originalName != null) {
        out.append(",");
        out.append(originalName);
      }

      out.append("]");
    }