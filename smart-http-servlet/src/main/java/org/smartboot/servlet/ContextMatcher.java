package org.smartboot.servlet;

import org.smartboot.http.utils.StringUtils;
import org.smartboot.servlet.util.LRUCache;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/16
 */
public class ContextMatcher {
    private List<DeploymentRuntime> runtimes = new ArrayList<>();
    private DeploymentRuntime defaultRuntime;
    private LRUCache<String, DeploymentRuntime> contextCache = new LRUCache<>();

    public ContextMatcher(DeploymentRuntime defaultRuntime) {
        this.defaultRuntime = defaultRuntime;
    }

    public DeploymentRuntime matchRuntime(String servletPath) {
        DeploymentRuntime runtime = contextCache.get(servletPath);
        if (runtime != null) {
            return runtime;
        }
        for (DeploymentRuntime matchRuntime : runtimes) {
            if (StringUtils.startsWith(servletPath, matchRuntime.getDeploymentInfo().getContextPath())) {
                runtime = matchRuntime;
                break;
            }
        }
        if (runtime == null) {
            runtime = defaultRuntime;
        }
        contextCache.put(servletPath, runtime);
        return runtime;
    }

    public DeploymentRuntime getDefaultRuntime() {
        return defaultRuntime;
    }

    public void setDefaultRuntime(DeploymentRuntime defaultRuntime) {
        this.defaultRuntime = defaultRuntime;
    }

    public void addRuntime(DeploymentRuntime runtime) {
        runtimes.add(runtime);
    }
}
