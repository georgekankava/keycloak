/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.keycloak.testsuite.forms;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.keycloak.events.Details;
import org.keycloak.models.OTPPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.HmacOTP;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.testsuite.AssertEvents;
import org.keycloak.testsuite.pages.AppPage;
import org.keycloak.testsuite.pages.AppPage.RequestType;
import org.keycloak.testsuite.pages.LoginPage;
import org.keycloak.testsuite.pages.LoginTotpPage;
import org.keycloak.testsuite.rule.GreenMailRule;
import org.keycloak.testsuite.rule.KeycloakRule;
import org.keycloak.testsuite.rule.KeycloakRule.KeycloakSetup;
import org.keycloak.testsuite.rule.WebResource;
import org.keycloak.testsuite.rule.WebRule;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.util.Collections;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LoginHotpTest {

    public static OTPPolicy policy;
    @ClassRule
    public static KeycloakRule keycloakRule = new KeycloakRule(new KeycloakSetup() {

        @Override
        public void config(RealmManager manager, RealmModel defaultRealm, RealmModel appRealm) {
            UserModel user = manager.getSession().users().getUserByUsername("test-user@localhost", appRealm);
            policy = appRealm.getOTPPolicy();
            policy.setType(UserCredentialModel.HOTP);
            policy.setLookAheadWindow(2);
            appRealm.setOTPPolicy(policy);

            UserCredentialModel credentials = new UserCredentialModel();
            credentials.setType(CredentialRepresentation.HOTP);
            credentials.setValue("hotpSecret");
            user.updateCredential(credentials);

            user.setOtpEnabled(true);
            appRealm.setEventsListeners(Collections.singleton("dummy"));
        }

    });

    @Rule
    public AssertEvents events = new AssertEvents(keycloakRule);

    @Rule
    public WebRule webRule = new WebRule(this);

    @Rule
    public GreenMailRule greenMail = new GreenMailRule();

    @WebResource
    protected WebDriver driver;

    @WebResource
    protected AppPage appPage;

    @WebResource
    protected LoginPage loginPage;

    @WebResource
    protected LoginTotpPage loginTotpPage;

    private HmacOTP otp = new HmacOTP(policy.getDigits(), policy.getAlgorithm(), policy.getLookAheadWindow());

    private int lifespan;

    private static int counter = 0;

    @Before
    public void before() throws MalformedURLException {
        otp = new HmacOTP(policy.getDigits(), policy.getAlgorithm(), policy.getLookAheadWindow());
    }

    @Test
    public void loginWithHotpFailure() throws Exception {
        loginPage.open();
        loginPage.login("test-user@localhost", "password");

        loginTotpPage.assertCurrent();

        loginTotpPage.login("123456");
        loginTotpPage.assertCurrent();
        Assert.assertEquals("Invalid authenticator code.", loginPage.getError());

        //loginPage.assertCurrent();  // Invalid authenticator code.
        //Assert.assertEquals("Invalid username or password.", loginPage.getError());

        events.expectLogin().error("invalid_user_credentials").session((String) null)
                .removeDetail(Details.CONSENT)
                .assertEvent();
    }

    @Test
    public void loginWithMissingHotp() throws Exception {
        loginPage.open();
        loginPage.login("test-user@localhost", "password");

        loginTotpPage.assertCurrent();

        loginTotpPage.login(null);
        loginTotpPage.assertCurrent();
        Assert.assertEquals("Invalid authenticator code.", loginPage.getError());

        //loginPage.assertCurrent();  // Invalid authenticator code.
        //Assert.assertEquals("Invalid username or password.", loginPage.getError());

        events.expectLogin().error("invalid_user_credentials").session((String) null)
                .removeDetail(Details.CONSENT)
                .assertEvent();
    }

    @Test
    public void loginWithHotpSuccess() throws Exception {
        loginPage.open();
        loginPage.login("test-user@localhost", "password");

        loginTotpPage.assertCurrent();

        loginTotpPage.login(otp.generateHOTP("hotpSecret", counter++));

        Assert.assertEquals(RequestType.AUTH_RESPONSE, appPage.getRequestType());

        events.expectLogin().assertEvent();
    }

    @Test
    public void loginWithHotpInvalidPassword() throws Exception {
        loginPage.open();
        loginPage.login("test-user@localhost", "invalid");

        loginPage.assertCurrent();

        Assert.assertEquals("Invalid username or password.", loginPage.getError());

        events.expectLogin().error("invalid_user_credentials").session((String) null)
                .removeDetail(Details.CONSENT)
                .assertEvent();
    }
}
