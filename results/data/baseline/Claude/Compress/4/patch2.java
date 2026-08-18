public void close() throws IOException {
        try {
            finish();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            } else if (out != null) {
                out.close();
            }
        }
    }