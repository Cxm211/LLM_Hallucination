private void handleBlockComment(Comment comment) {
    // Warn on block comments (/* ... */) that contain an @-annotation but are not JSDoc (/** ... */)
    String value = comment.getValue();
    if (value != null) {
      // JSDoc comments' contents start with '*', while plain block comments do not.
      if (value.indexOf('@') != -1 && (value.isEmpty() || value.charAt(0) != '*')) {
        errorReporter.warning(
            SUSPICIOUS_COMMENT_WARNING,
            sourceName,
            comment.getLineno(), "", 0);
      }
    }
  }