package org.apache.ivory.resource.channel;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.apache.ivory.IvoryException;
import org.apache.ivory.security.CurrentUser;
import org.apache.ivory.util.DeploymentProperties;
import org.apache.ivory.util.RuntimeProperties;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Properties;

public class HTTPChannel extends AbstractChannel {
    private static final Logger LOG = Logger.getLogger(HTTPChannel.class);

    private static final String REMOTE_USER = "Remote-User";

    private static final HttpServletRequest DEFAULT_NULL_REQUEST =
            new NullServletRequest();

    private static final Properties deploymentProperties = DeploymentProperties.get();

    private Class service;
    private String urlPrefix;

    public void init(String colo, String serviceName) throws IvoryException {
        String prefixPath = deploymentProperties.
                getProperty(serviceName + ".path");
        String ivoryEndPoint = RuntimeProperties.get().
                getProperty("ivory." + colo + ".endpoint");
        urlPrefix = ivoryEndPoint + "/" + prefixPath;

        try {
            String proxyClassName = deploymentProperties.
                    getProperty(serviceName + ".proxy");
            service = Class.forName(proxyClassName);
            LOG.info("Service: " + serviceName + ", url = " + urlPrefix);
        } catch (Exception e) {
            throw new IvoryException("Unable to initialize channel for "
                    + serviceName, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T invoke(String methodName, Object... args)
            throws IvoryException {

        Method method = getMethod(service, methodName, args);
        String url = urlPrefix + "/" + pathValue(method, args);
        LOG.debug("Executing " + url);

        HttpServletRequest incomingRequest = getIncomingRequest(args);
        String httpMethod = getHttpMethod(method);
        String mimeType = getConsumes(method);
        String accept = getProduces(method);
        String user = CurrentUser.getUser();

        try {
            ClientResponse response = Client.create(new DefaultClientConfig())
                    .resource(UriBuilder.fromUri(url).build())
                    .header(REMOTE_USER, user).accept(accept)
                    .type(mimeType).method(httpMethod, ClientResponse.class,
                            incomingRequest.getInputStream());

            Family status = response.getClientResponseStatus().getFamily();
            if (status == Family.INFORMATIONAL || status == Family.REDIRECTION ||
                    status == Family.SUCCESSFUL) {
                return (T)response.getEntity(method.getReturnType());
            } else {
                LOG.error("Request failed: " + response.getClientResponseStatus().getStatusCode());
                throw new IvoryException(response.getEntity(String.class));
            }
        } catch (IOException e) {
            LOG.error("Request failed", e);
            throw new IvoryException(e);
        }
    }

    private String pathValue(Method method, Object... args)
            throws IvoryException {

        Path pathParam = method.getAnnotation(Path.class);
        if (pathParam == null) {
            throw new IvoryException("No path param mentioned for " + method);
        }
        String pathValue = pathParam.value();

        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        StringBuilder queryString = new StringBuilder("?");
        for (int index = 0; index < args.length; index++) {
            if (args[index] instanceof String) {
                String arg = (String) args[index];
                for (int annotation = 0; annotation < paramAnnotations[index].length; annotation++) {
                    Annotation paramAnnotation = paramAnnotations[index][annotation];
                    String annotationClass = paramAnnotation.annotationType().getName();

                    if (annotationClass.equals(QueryParam.class.getName())) {
                        queryString.append(getAnnotationValue(paramAnnotation, "value")).
                                append('=').append(arg).append("&");
                    } else if (annotationClass.equals(PathParam.class.getName())) {
                        pathValue = pathValue.replace("{" +
                                getAnnotationValue(paramAnnotation, "value") + "}", arg);
                    }
                }
            }
        }
        return pathValue + queryString.toString();
    }

    private String getAnnotationValue(Annotation paramAnnotation,
                                      String annotationAttribute)
            throws IvoryException {

        try {
            return String.valueOf(paramAnnotation.annotationType().
                    getMethod(annotationAttribute).invoke(paramAnnotation));
        } catch (Exception e) {
            throw new IvoryException("Unable to get attribute value for " +
                    paramAnnotation + "[" + annotationAttribute + "]");
        }
    }

    private HttpServletRequest getIncomingRequest(Object... args) {
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        return DEFAULT_NULL_REQUEST;
    }

    private String getHttpMethod(Method method) {
        PUT put = method.getAnnotation(PUT.class);
        if (put != null) return HttpMethod.PUT;

        POST post = method.getAnnotation(POST.class);
        if (post != null) return HttpMethod.POST;

        DELETE delete = method.getAnnotation(DELETE.class);
        if (delete != null) return HttpMethod.DELETE;

        return HttpMethod.GET;
    }

    private String getConsumes(Method method) {
        Consumes consumes = method.getAnnotation(Consumes.class);
        if (consumes.value() == null) {
            return MediaType.TEXT_PLAIN;
        }
        return consumes.value()[0];
    }

    private String getProduces(Method method) {
        Produces produces = method.getAnnotation(Produces.class);
        if (produces.value() == null) {
            return MediaType.WILDCARD;
        }
        return produces.value()[0];
    }

}
