package au.rmit.ap2.carehome.service;

import au.rmit.ap2.carehome.exceptions.AuthorizationException;
import au.rmit.ap2.carehome.model.Role;
import au.rmit.ap2.carehome.model.Staff;

public class AuthService {
    public void require(Staff s, Role role){ if (s==null || s.getRole()!=role) throw new AuthorizationException("Requires role: "+role); }
    public void requireAny(Staff s, Role... roles){
        for(Role r:roles) if (s!=null && s.getRole()==r) return;
        throw new AuthorizationException("Not authorized");
    }
}
