package au.rmit.ap2.carehome.service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<String> entries = new ArrayList<>();
    public void log(String staffId, String action){ entries.add(LocalDateTime.now()+" ["+staffId+"] "+action); }
    public List<String> last(int n){ int from=Math.max(0,entries.size()-n); return entries.subList(from, entries.size()); }
}
