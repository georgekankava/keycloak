package org.keycloak.testsuite.adapter.page;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.test.api.ArquillianResource;

import java.net.URL;

/**
 * @author mhajas
 */
public class SalesPostSigEmailServlet extends SAMLServletWithLogout {
    public static final String DEPLOYMENT_NAME = "sales-post-sig-email";

    @ArquillianResource
    @OperateOnDeployment(DEPLOYMENT_NAME)
    private URL url;

    @Override
    public URL getInjectedUrl() {
        return url;
    }
}
