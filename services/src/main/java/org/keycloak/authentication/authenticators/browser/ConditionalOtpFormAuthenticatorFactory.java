package org.keycloak.authentication.authenticators.browser;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

import static java.util.Arrays.asList;
import static org.keycloak.authentication.authenticators.browser.ConditionalOtpFormAuthenticator.*;
import static org.keycloak.provider.ProviderConfigProperty.*;

/**
 * An {@link AuthenticatorFactory} for {@link ConditionalOtpFormAuthenticator}s.
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class ConditionalOtpFormAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "auth-conditional-otp-form";

    public static final ConditionalOtpFormAuthenticator SINGLETON = new ConditionalOtpFormAuthenticator();

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.OPTIONAL,
            AuthenticationExecutionModel.Requirement.DISABLED};

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
        //NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        //NOOP
    }

    @Override
    public void close() {
        //NOOP
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return UserCredentialModel.TOTP;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }


    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return "Conditional OTP Form";
    }

    @Override
    public String getHelpText() {
        return "Validates a OTP on a separate OTP form. Only shown if required based on the configured conditions.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        ProviderConfigProperty forceOtpUserAttribute = new ProviderConfigProperty();
        forceOtpUserAttribute.setType(STRING_TYPE);
        forceOtpUserAttribute.setName(OTP_CONTROL_USER_ATTRIBUTE);
        forceOtpUserAttribute.setLabel("OTP control User Attribute");
        forceOtpUserAttribute.setHelpText("The name of the user attribute to explicitly control OTP auth. " +
                "If attribute value is 'force' then OTP is always required. " +
                "If value is 'skip' the OTP auth is skipped. Otherwise this check is ignored.");

        ProviderConfigProperty skipOtpRole = new ProviderConfigProperty();
        skipOtpRole.setType(ROLE_TYPE);
        skipOtpRole.setName(SKIP_OTP_ROLE);
        skipOtpRole.setLabel("Skip OTP for Role");
        skipOtpRole.setHelpText("OTP is always skipped if user has the given Role.");

        ProviderConfigProperty forceOtpRole = new ProviderConfigProperty();
        forceOtpRole.setType(ROLE_TYPE);
        forceOtpRole.setName(FORCE_OTP_ROLE);
        forceOtpRole.setLabel("Force OTP for Role");
        forceOtpRole.setHelpText("OTP is always required if user has the given Role.");

        ProviderConfigProperty skipOtpForHttpHeader = new ProviderConfigProperty();
        skipOtpForHttpHeader.setType(STRING_TYPE);
        skipOtpForHttpHeader.setName(SKIP_OTP_FOR_HTTP_HEADER);
        skipOtpForHttpHeader.setLabel("Skip OTP for Header");
        skipOtpForHttpHeader.setHelpText("OTP is skipped if a HTTP request header does matches the given pattern." +
                "Can be used to specify trusted networks via: X-Forwarded-Host: (1.2.3.4|1.2.3.5)." +
                "In this case requests from 1.2.3.4 and 1.2.3.5 come from a trusted source.");
        skipOtpForHttpHeader.setDefaultValue("");

        ProviderConfigProperty forceOtpForHttpHeader = new ProviderConfigProperty();
        forceOtpForHttpHeader.setType(STRING_TYPE);
        forceOtpForHttpHeader.setName(FORCE_OTP_FOR_HTTP_HEADER);
        forceOtpForHttpHeader.setLabel("Force OTP for Header");
        forceOtpForHttpHeader.setHelpText("OTP required if a HTTP request header matches the given pattern.");
        forceOtpForHttpHeader.setDefaultValue("");

        ProviderConfigProperty defaultOutcome = new ProviderConfigProperty();
        defaultOutcome.setType(LIST_TYPE);
        defaultOutcome.setName(DEFAULT_OTP_OUTCOME);
        defaultOutcome.setLabel("Fallback OTP handling");
        defaultOutcome.setDefaultValue(asList(SKIP, FORCE));
        defaultOutcome.setHelpText("What to do in case of every check abstains. Defaults to force OTP authentication.");

        return asList(forceOtpUserAttribute, skipOtpRole, forceOtpRole, skipOtpForHttpHeader, forceOtpForHttpHeader, defaultOutcome);
    }
}
