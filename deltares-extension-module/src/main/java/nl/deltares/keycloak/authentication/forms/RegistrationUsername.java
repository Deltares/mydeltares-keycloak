package nl.deltares.keycloak.authentication.forms;

import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.events.Details;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

public class RegistrationUsername implements FormAction, FormActionFactory {
    private static final String PROVIDER_ID = "registration-deltares-user-action";

    @Override
    public String getHelpText() {
        return "Checks username and if missing auto generates.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public void validate(ValidationContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        context.getEvent().detail(Details.REGISTER_METHOD, "form");

        String username = formData.getFirst(RegistrationPage.FIELD_USERNAME);
        context.getEvent().detail(Details.USERNAME, username);

        if (username == null || username.trim().length() == 0) {
            String email = formData.getFirst(RegistrationPage.FIELD_EMAIL);
            String newUserName = createUserName(email, context);
            if (newUserName != null) {
                formData.putSingle(Details.USERNAME, newUserName);
            }
        } else {
            int i = username.indexOf('\\');
            if (i > 0){
                //remove directory\ string
                String strippedUsername = username.substring(i+1);
                formData.putSingle(Details.USERNAME, strippedUsername);
            }
        }
        context.success();
    }

    private String createUserName(String email, ValidationContext context) {

        if (email == null) return null;
        int i = email.indexOf('@');
        String userName;
        if (i > 0){
            //remove domain
            userName =  email.substring(0, i);
        } else {
            userName = email;
        }

        UserProvider users = context.getSession().users();
        i = 0;
        String validUserName = userName;
        while (users.getUserByUsername(validUserName, context.getRealm()) != null){
            validUserName = userName + '_' + String.valueOf(i++);
        }
        return validUserName;
    }

    @Override
    public void success(FormContext context) {
//        UserModel user = context.getUser();
//        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
//        user.setUsername(formData.getFirst(RegistrationPage.FIELD_USERNAME));
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        // complete
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }


    @Override
    public void close() {

    }

    @Override
    public String getDisplayType() {
        return "Deltares Username Validate ";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }
    @Override
    public FormAction create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
