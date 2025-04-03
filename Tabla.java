import java.util.*;

class Tabla {
    private final int nMarcos;
    private final Map<Integer, Page> tabla;
    private final Queue<Integer> marcos;

    public Tabla(int nMarcos) {
        this.nMarcos = nMarcos;
        this.tabla = new HashMap<>();
        this.marcos = new LinkedList<>();
    }

    public synchronized boolean cargarPagina(int pagina, String accion) {
        if (tabla.containsKey(pagina)) {
            
            Page page = tabla.get(pagina);
            page.r = true;
            
            return true;
        
        } else {
            if (tabla.size() >= nMarcos) {
                reemplazar();
            }

            Page newPage;
            if(accion.equals("R")){
                newPage = new Page(pagina, false);
            } else {
                newPage = new Page(pagina, true);
            }

            tabla.put(pagina, newPage);
            marcos.add(pagina);
        
            return false;
        }
    }

    private synchronized void reemplazar() {
        int pageToReplace = nru();
        tabla.remove(pageToReplace);
        marcos.remove(pageToReplace);
    }

    private synchronized int nru() {
        Integer candidato = null;
        
        for (int pageId : marcos) {
            Page page = tabla.get(pageId);
        
            if(!page.r && !page.m) {
                return pageId;
            }
        
            if(!page.r && page.m && candidato == null) {
                candidato = pageId;
            }
        }
        
        return candidato != null ? candidato : marcos.peek();
    }

    public synchronized void resetear() {
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
