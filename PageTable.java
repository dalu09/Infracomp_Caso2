import java.util.ArrayList;
import java.util.HashMap;

public class PageTable {
    private HashMap<Integer, Page> pagesTable = new HashMap<>();
    private ArrayList<Integer> frames = new ArrayList<>();
    private int maxFrames;

    public PageTable(int maxFrames) {
        this.maxFrames = maxFrames;
    }

    class Page {
        boolean referenced;
        boolean modified;

        Page() {
            referenced = false;
            modified = false;
        }
    }

    public synchronized boolean loadPage(int page) {
        if (pagesTable.containsKey(page)) {
            Page currentPage = pagesTable.get(page);
            currentPage.referenced = true;
            return true;
        } else {
            if (frames.size() < maxFrames) {
                frames.add(page);
                pagesTable.put(page, new Page());
            } else {
                replacePage(page);
            }
            return false;
        }
    }

    public synchronized void replacePage(int page) {
        int paginaReemplazar = selectPageToReplace();
        frames.set(frames.indexOf(paginaReemplazar), page);
        pagesTable.put(page, new Page());
        pagesTable.remove(paginaReemplazar);
    }

    public synchronized void categorizePages() {
        for (Integer page : pagesTable.keySet()) {
            Page p = pagesTable.get(page);
            p.referenced = false;
        }
    }

    public synchronized int selectPageToReplace() {
        Integer mejorOpcion = null;
        int mejorClase = 5;

        for (Integer page : frames) {
            Page p = pagesTable.get(page);
            int clase = (p.referenced ? 2 : 0) + (p.modified ? 1 : 0);

            if (clase < mejorClase) {
                mejorClase = clase;
                mejorOpcion = page;
            }
        }
        return mejorOpcion;
    }
}
