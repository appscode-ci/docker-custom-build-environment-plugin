package com.appscode.ci.plugins.blueprint;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildBadgeAction;
import hudson.model.EnvironmentContributingAction;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to determine if launcher has to be decorated to execute in container, after SCM checkout completed.
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class BuiltInContainer implements BuildBadgeAction, EnvironmentContributingAction {

    /* package */ String image;

    /* package */ transient String container;

    private transient String userId;
    private transient boolean enable;
    private transient Docker docker;
    private List<Integer> ports = new ArrayList<Integer>();
    private Map<String,String> volumes = new HashMap<String,String>();

    public BuiltInContainer() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void enable() {
        enable(true);
    }

    public void enable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnabled() {
        return enable;
    }

    public String getIconFileName() {
        return "/plugin/appscode-blueprint/docker-badge.png";
    }

    public String getImage() {
        return image;
    }

    public String getDisplayName() {
        return "built inside docker container";
    }

    public String getUrlName() {
        return "/docker";
    }

    Docker getDocker() {
        return docker;
    }

    public void setDocker(Docker docker) {
        this.docker = docker;
    }

    public boolean tearDown() throws IOException, InterruptedException {
        if (container != null) {
            enable = false;
            docker.kill(container);
        }
        return true;

    }

    public List<Integer> getPorts() {
        return ports;
    }

    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        if (enable && container != null) {
            env.put("BUILD_CONTAINER_ID", container);
        }
    }

    public void bindMount(String path) {
        volumes.put(path, path);
    }

    public void bindMount(String hostPath, String path) {
        volumes.put(hostPath, path);
    }

    public Map<String, String> getVolumes() {
        return volumes;
    }


    public @Nonnull Map<Integer, Integer> getPortsMap() {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (Integer port : ports) {
            map.put(port, port);
        }
        return map;
    }

    public @Nonnull Map<String, String> getVolumes(AbstractBuild build) throws IOException, InterruptedException {
        final EnvVars environment = build.getEnvironment(TaskListener.NULL);
        Map<String, String> map = new HashMap<String, String>(volumes);
        for (Map.Entry<String, String> e : volumes.entrySet()) {
            map.put(environment.expand(e.getKey()), environment.expand(e.getValue()));
        }
        return map;
    }
}