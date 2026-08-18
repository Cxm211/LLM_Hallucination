// org/apache/commons/compress/archivers/tar/TarArchiveEntryTest.java
package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.Test;

public class TarArchiveEntryTest {

    @Test
    public void isDirectoryIgnoresTrailingSlashForPaxHeaders() {
        TarArchiveEntry e = new TarArchiveEntry("ignored/");
        e.setLinkFlag((byte) 'x'); // PAX extended header
        assertFalse(e.isDirectory());
    }
}
