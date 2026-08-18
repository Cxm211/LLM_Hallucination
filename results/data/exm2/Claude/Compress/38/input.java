    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR) {
            return true;
        }

        if (getName().endsWith("/")) {
            return true;
        }

        return false;
    }

// trigger testcase
@Test
    public void survivesPaxHeaderWithNameEndingInSlash() throws Exception {
        final TarArchiveInputStream is = getTestStream("/COMPRESS-356.tar");
        try {
            final TarArchiveEntry entry = is.getNextTarEntry();
            assertEquals("package/package.json", entry.getName());
            assertNull(is.getNextTarEntry());
        } finally {
            is.close();
        }
    }
