package org.smartboot.http.server.waf;

import java.util.HashSet;
import java.util.Set;

public class WafConfiguration {
    private Set<String> allowMethods = new HashSet<>();
    private Set<String> denyMethods = new HashSet<>();

    public Set<String> getAllowMethods() {
        return allowMethods;
    }

    public void setAllowMethods(Set<String> allowMethods) {
        this.allowMethods = allowMethods;
    }

    public Set<String> getDenyMethods() {
        return denyMethods;
    }

    public void setDenyMethods(Set<String> denyMethods) {
        this.denyMethods = denyMethods;
    }
}
