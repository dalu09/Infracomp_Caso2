import java.util.*;

class PageTable {
    private final int numFrames;
    private final Map<Integer, Page> pageTable;
    private final Queue<Integer> frameQueue;

    public PageTable(int numFrames) {
        this.numFrames = numFrames;
        this.pageTable = new HashMap<>();
        this.frameQueue = new LinkedList<>();
    }

    public synchronized boolean loadPage(int virtualPage, String accion) {
        if (pageTable.containsKey(virtualPage)) {
            Page page = pageTable.get(virtualPage);
            page.referenced = true;
            return true;
        } else {
            if (pageTable.size() >= numFrames) {
                replacePage();
            }

            Page newPage;
            if(accion.equals("R")){
                newPage = new Page(virtualPage, false);
            } else {
                newPage = new Page(virtualPage, true);
            }
            
            pageTable.put(virtualPage, newPage);
            frameQueue.add(virtualPage);
            return false;
        }
    }

    private synchronized void replacePage() {
        int pageToReplace = nruReplacement();
        pageTable.remove(pageToReplace);
        frameQueue.remove(pageToReplace);
    }

    private synchronized int nruReplacement() {
        Integer candidate = null;
        for (int pageId : frameQueue) {
            Page page = pageTable.get(pageId);
            if (!page.referenced && !page.modified) {
                return pageId;
            }
            if (!page.referenced && page.modified && candidate == null) {
                candidate = pageId;
            }
        }
        
        return candidate != null ? candidate : frameQueue.peek();
    }

    public synchronized void resetReferencedBits() {
        for (Page page : pageTable.values()) {
            page.referenced = false;
        }
    }
}

class Page {
    int id;
    boolean referenced;
    boolean modified;

    public Page(int id, boolean m) {
        this.id = id;
        this.referenced = true;
        this.modified = m;
    }
}
