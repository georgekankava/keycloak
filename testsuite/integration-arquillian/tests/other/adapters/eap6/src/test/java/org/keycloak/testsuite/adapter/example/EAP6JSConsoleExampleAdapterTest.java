package org.keycloak.testsuite.adapter.example;

import org.keycloak.testsuite.arquillian.annotation.AdapterLibsLocationProperty;
import org.keycloak.testsuite.arquillian.annotation.AppServerContainer;
import org.junit.Ignore;
/**
 * @author tkyjovsk
 */
@AppServerContainer("app-server-eap6")
@AdapterLibsLocationProperty("adapter.libs.eap6")
@Ignore //jsconsole example has hardcoded relative path to keycloak.js
public class EAP6JSConsoleExampleAdapterTest extends AbstractJSConsoleExampleAdapterTest {

}
