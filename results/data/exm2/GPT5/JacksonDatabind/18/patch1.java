public void close() throws IOException {
        if (_parser != null) {
            try {
                _parser.close();
            } finally {
                _parser = null;
            }
        }
}