package com.javaxpert.demos.jenkins.dummyplugin;

import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.Normalizer;
import java.util.logging.Logger;

import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

public class HelloWorldBuilder extends Builder implements SimpleBuildStep {
    private final String url;
    private final static Logger LOGGER = Logger.getLogger(HelloWorldBuilder.class.getName());

    @DataBoundConstructor
    public HelloWorldBuilder(String url) {
        LOGGER.info("Dummy plugin constructor ");
        this.url=url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        LOGGER.info("performing build step for Dummy Plugin");
        listener.getLogger().println("performing dummy plugin action..Url ="+ url);
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(url).build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        DockerHttpClient.Request request = DockerHttpClient.Request.builder()
                .method(DockerHttpClient.Request.Method.GET)
                .path("/version")
                .build();
        DockerHttpClient.Response response = httpClient.execute(request);
        int error = response.getStatusCode();
        LOGGER.info("received status code =" + error);
        response.close();
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckUrl(@QueryParameter String value) {
            LOGGER.info("checking url parameter with value="+ value);
            if(value==null || value.isEmpty()){
                return FormValidation.error("url is not filled in...");
            }
            if(!value.trim().startsWith("tcp://")){
                return FormValidation.error("url does not seeem to point to any docker  host");
            }
            LOGGER.info("validating formular for Dummy plugin");
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Dummy plugin";
        }
    }

}
