public void close() throws IOException {
        if (!closed) {
            try {
                finish();
            } finally {
                try {
                    buffer.close();
                } finally {
                    out.close();
                    closed = true;
                }
            }
        }
    }