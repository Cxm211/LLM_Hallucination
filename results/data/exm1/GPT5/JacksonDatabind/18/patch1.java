public void close() throws IOException {
            if (_parser != null) {
                if (_closeParser) {
                    _parser.close();
                }
                _parser = null;
            }
    }