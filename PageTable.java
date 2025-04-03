import java.util.*;

class Tabla {
    private final int numFrames;
    private final Map<Integer, Page> tabla;
    private final Queue<Integer> frameQueue;

    public Tabla(int numFrames) {
        this.numFrames = numFrames;
        this.tabla = new HashMap<>();
        this.frameQueue = new LinkedList<>();
    }

    public synchronized boolean loadPage(int virtualPage, String accion) {
        if (tabla.containsKey(virtualPage)) {
            
            Page page = tabla.get(virtualPage);
            page.r = true;
            
            return true;
        
        } else {
            if (tabla.size() >= numFrames) {
                replacePage();
            }

            Page newPage;
            if(accion.equals("R")){
                newPage = new Page(virtualPage, false);
            } else {
                newPage = new Page(virtualPage, true);
            }

            tabla.put(virtualPage, newPage);
            frameQueue.add(virtualPage);
        
            return false;
        }
    }

    private synchronized void replacePage() {
        int pageToReplace = nruReplacement();
        tabla.remove(pageToReplace);
        frameQueue.remove(pageToReplace);
    }

    private synchronized int nruReplacement() {
        Integer candidate = null;
        
        for (int pageId : frameQueue) {
            Page page = tabla.get(pageId);
        
            if(!page.r && !page.m) {
                return pageId;
            }
        
            if(!page.r && page.m && candidate == null) {
                candidate = pageId;
            }
        }
        
        return candidate != null ? candidate : frameQueue.peek();
    }

    public synchronized void resetReferencedBits() {
        for (Page page : tabla.values()) {
            page.r = false;
        }
    }
}

class Page {
    int id;
    boolean r;
    boolean m;

    public Page(int id, boolean m) {
        this.id = id;
        this.r = true;
        this.m = m;
    }
}
