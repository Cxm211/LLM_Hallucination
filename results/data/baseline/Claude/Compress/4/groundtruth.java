// ===== FIXED org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream :: close() [lines 334-339] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-4-fixed/src/main/java/org/apache/commons/compress/archivers/cpio/CpioArchiveOutputStream.java =====
    public void close() throws IOException {
        if (!this.closed) {
            out.close();
            this.closed = true;
        }
    }

// ===== FIXED org.apache.commons.compress.archivers.tar.TarArchiveOutputStream :: close() [lines 124-130] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-4-fixed/src/main/java/org/apache/commons/compress/archivers/tar/TarArchiveOutputStream.java =====
    public void close() throws IOException {
        if (!closed) {
            buffer.close();
            out.close();
            closed = true;
        }
    }

// ===== FIXED org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream :: close() [lines 529-536] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-4-fixed/src/main/java/org/apache/commons/compress/archivers/zip/ZipArchiveOutputStream.java =====
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
        if (out != null) {
            out.close();
        }
    }

// ===== FIXED org.apache.commons.compress.changes.ChangeSetPerformer :: perform(ArchiveInputStream, ArchiveOutputStream) [lines 67-130] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-4-fixed/src/main/java/org/apache/commons/compress/changes/ChangeSetPerformer.java =====
    public ChangeSetResults perform(ArchiveInputStream in, ArchiveOutputStream out)
            throws IOException {
        ChangeSetResults results = new ChangeSetResults();
        
        Set workingSet = new LinkedHashSet(changes);
        
        for (Iterator it = workingSet.iterator(); it.hasNext();) {
            Change change = (Change) it.next();

            if (change.type() == Change.TYPE_ADD && change.isReplaceMode()) {
                copyStream(change.getInput(), out, change.getEntry());
                it.remove();
                results.addedFromChangeSet(change.getEntry().getName());
            }
        }

        ArchiveEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            boolean copy = true;

            for (Iterator it = workingSet.iterator(); it.hasNext();) {
                Change change = (Change) it.next();

                final int type = change.type();
                final String name = entry.getName();
                if (type == Change.TYPE_DELETE && name != null) {
                    if (name.equals(change.targetFile())) {
                        copy = false;
                        it.remove();
                        results.deleted(name);
                        break;
                    }
                } else if(type == Change.TYPE_DELETE_DIR && name != null) {
                    if (name.startsWith(change.targetFile() + "/")) {
                        copy = false;
                        results.deleted(name);
                        break;
                    }
                }
            }

            if (copy) {
                if (!isDeletedLater(workingSet, entry) && !results.hasBeenAdded(entry.getName())) {
                    copyStream(in, out, entry);
                    results.addedFromStream(entry.getName());
                }
            }
        }
        
        // Adds files which hasn't been added from the original and do not have replace mode on
        for (Iterator it = workingSet.iterator(); it.hasNext();) {
            Change change = (Change) it.next();

            if (change.type() == Change.TYPE_ADD && 
                !change.isReplaceMode() && 
                !results.hasBeenAdded(change.getEntry().getName())) {
                copyStream(change.getInput(), out, change.getEntry());
                it.remove();
                results.addedFromChangeSet(change.getEntry().getName());
            }
        }
        out.finish();
        return results;
    }
