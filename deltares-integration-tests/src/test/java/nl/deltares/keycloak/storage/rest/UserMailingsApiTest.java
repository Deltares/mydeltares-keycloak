package nl.deltares.keycloak.storage.rest;

import nl.deltares.keycloak.IntegrationTestCategory;
import nl.deltares.keycloak.KeycloakTestServer;
import nl.deltares.keycloak.storage.jpa.Mailing;
import nl.deltares.keycloak.storage.jpa.model.DataRequestManager;
import nl.deltares.keycloak.utils.KeycloakUtilsImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;

import java.io.*;
import java.util.List;

import static nl.deltares.keycloak.storage.rest.TestUtils.newUserRepresentation;

@Category(IntegrationTestCategory.class)
public class UserMailingsApiTest {

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp(){

        System.setProperty("csv_prefix", name.getMethodName());
        Assert.assertTrue(KeycloakTestServer.isRunning());
    }

    @Test
    public void adminApiGetSubscribedUserMailingsUnauthorized() {

        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();
        try  {
            keycloakUtils.getUserMailingsSubscriptionsAdminApi("subscribe-user2@test.nl");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error 403"));
        }

    }

    @Test
    public void adminApiGetSubscribedUserMailings() throws IOException {

        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getAdminKeycloakUtils();

        keycloakUtils.createUser(newUserRepresentation("subscribe-user2@test.nl"));

        List<UserMailingRepresentation> subscriptions = keycloakUtils.getUserMailingsSubscriptionsAdminApi("subscribe-user2@test.nl");
        Assert.assertTrue(subscriptions.isEmpty());

        try (StringWriter writer = new StringWriter()) {
            int status = keycloakUtils.subscribeUserMailingsAdminApi(writer, "db0be0a5-e96e-40b6-b687-fdb12304b69a", "subscribe-user2@test.nl");
            Assert.assertEquals(200, status);
            String subscribeUserMailings = writer.toString();
            System.out.println("adminApiSubscribeUserMailings: " + subscribeUserMailings);
            Assert.assertTrue(subscribeUserMailings.contains( "user subscribed for mailing"));
        }

        subscriptions = keycloakUtils.getUserMailingsSubscriptionsAdminApi("subscribe-user2@test.nl");
        Assert.assertTrue(subscriptions.size() == 1);
        Assert.assertEquals("db0be0a5-e96e-40b6-b687-fdb12304b69a", subscriptions.get(0).mailingId);

    }

    @Test
    public void adminApiSubscribeUserMailingsUnauthorized() {

        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();
        try (StringWriter writer = new StringWriter()) {
            keycloakUtils.subscribeUserMailingsAdminApi(writer, "db0be0a5-e96e-40b6-b687-fdb12304b69a", "subscribe-user1@test.nl");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error 403"));
        }

    }

    @Test
    public void adminApiSubscribeUserMailings() throws IOException {

        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getAdminKeycloakUtils();

        keycloakUtils.createUser(newUserRepresentation("subscribe-user1@test.nl"));

        try (StringWriter writer = new StringWriter()) {

            int status = keycloakUtils.subscribeUserMailingsAdminApi(writer, "db0be0a5-e96e-40b6-b687-fdb12304b69a", "subscribe-user1@test.nl");
            Assert.assertEquals(200, status);
            String subscribeUserMailings = writer.toString();
            System.out.println("adminApiSubscribeUserMailings: " + subscribeUserMailings);
            Assert.assertTrue(subscribeUserMailings.contains( "user subscribed for mailing"));
        }

        try (StringWriter writer = new StringWriter()) {
            int status = keycloakUtils.subscribeUserMailingsAdminApi(writer, "db0be0a5-e96e-40b6-b687-fdb12304b69a", "subscribe-user1@test.nl");
            Assert.assertEquals(200, status);
            String subscribeUserMailings = writer.toString();
            System.out.println("adminApiSubscribeUserMailings: " + subscribeUserMailings);
            Assert.assertTrue(subscribeUserMailings.contains( "user already subscribed for mailing"));
        }

    }

    @Test
    public void adminApiUnsubscribeUserMailingsUnauthorized() {

        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();
        try (StringWriter writer = new StringWriter()) {
            keycloakUtils.unsubscribeUserMailingsAdminApi(writer, "db0be0a5-e96e-40b6-b687-fdb12304b69a", "unsubscribe-user1@test.nl");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error 403"));
        }

    }

    @Test
    public void adminApiUnsubscribeUserMailings() throws IOException {

        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getAdminKeycloakUtils();

        keycloakUtils.createUser(newUserRepresentation("unsubscribe-user1@test.nl"));

        try (StringWriter writer = new StringWriter()) {

            int status = keycloakUtils.subscribeUserMailingsAdminApi(writer, "db0be0a5-e96e-40b6-b687-fdb12304b69a", "unsubscribe-user1@test.nl");
            Assert.assertEquals(200, status);
            String subscribeUserMailings = writer.toString();
            System.out.println("adminApiUnsubscribeUserMailings: " + subscribeUserMailings);
            Assert.assertTrue(subscribeUserMailings.contains( "user subscribed for mailing"));
        }

        try (StringWriter writer = new StringWriter()) {
            int status = keycloakUtils.unsubscribeUserMailingsAdminApi(writer, "db0be0a5-e96e-40b6-b687-fdb12304b69a", "unsubscribe-user1@test.nl");
            Assert.assertEquals(200, status);
            String subscribeUserMailings = writer.toString();
            System.out.println("adminApiUnsubscribeUserMailings: " + subscribeUserMailings);
            Assert.assertTrue(subscribeUserMailings.contains( "user unsubscribed for mailing"));
        }

    }

    @Test
    public void adminApiExportUserMailings() throws IOException {


        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getAdminKeycloakUtils();

        try (StringWriter writer = new StringWriter()) {
            int status = keycloakUtils.exportUserMailingsAdminApi(writer, "db0be0a5-e96e-40b6-b687-fdb12304b69a");
            Assert.assertEquals(200, status);
            String exportedUserMailings = writer.toString();
            System.out.println("adminApiExportUserMailings: " + exportedUserMailings);
            Assert.assertTrue(exportedUserMailings.contains( "User 1;export;export-usermailing1@test.nl;Mr;Test;Test;en;e-mail"));
            Assert.assertTrue(exportedUserMailings.contains( "User 2;export;export-usermailing2@test.nl;Ms;Deltares;The Netherlands;en;e-mail"));
        }

    }

    @Test
    public void adminApiExportUserMailingsUnauthorized() {

        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();

        try (StringWriter writer = new StringWriter()){
            keycloakUtils.exportUserMailingsAdminApi(writer, "db0be0a5-e96e-40b6-b687-fdb12304b69a");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error 403"));
        }

    }

    @Test
    public void adminApiImportUserMailings() throws IOException {

        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getAdminKeycloakUtils();
        try (StringWriter writer = new StringWriter()) {
            keycloakUtils.exportUserMailingsAdminApi(writer, "3d4baf14-6088-400c-83c3-c20f14e63d51");
            String exportedUserMailings = writer.toString();
            Assert.assertFalse(exportedUserMailings.contains( "User 1;import;import-usermailing1@test.nl;Mr;Test;Test;en;e-mail"));
            Assert.assertFalse(exportedUserMailings.contains( "User 2;import;import-usermailing2@test.nl;Ms;Deltares;The Netherlands;en;e-mail"));
        }

        String userIds = "d05d4b89-59a9-4343-ae55-363234d1ac14\n" +
                "fc864c55-afca-4a63-b5ca-ee055f550ad8";

        try (Reader reader = new StringReader(userIds)) {
            int status = keycloakUtils.setMailingForUserIdsAdminApi("3d4baf14-6088-400c-83c3-c20f14e63d51", reader);
            Assert.assertEquals(200, status);
        }

        DataRequestManager.getInstance().clear();
        try (StringWriter writer = new StringWriter()) {
            keycloakUtils.exportUserMailingsAdminApi(writer, "3d4baf14-6088-400c-83c3-c20f14e63d51");
            String exportedUserMailings = writer.toString();
            System.out.println("adminApiImportUserMailings: " + exportedUserMailings);
            Assert.assertTrue(exportedUserMailings.contains( "User 1;import;import-usermailing1@test.nl;Mr;Test;Test;en;e-mail"));
            Assert.assertTrue(exportedUserMailings.contains( "User 2;import;import-usermailing2@test.nl;Ms;Deltares;The Netherlands;en;e-mail"));
        }

    }

    @Test
    public void adminApiImportUserMailingsUnauthorized() {

        String userIds = "d05d4b89-59a9-4343-ae55-363234d1ac14\n" +
                "fc864c55-afca-4a63-b5ca-ee055f550ad8";

        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getViewerKeycloakUtils();
        try (Reader reader = new StringReader(userIds)) {
            keycloakUtils.setMailingForUserIdsAdminApi("3d4baf14-6088-400c-83c3-c20f14e63d51", reader);
            Assert.fail();
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error 403"));
        }

    }

    @Test
    public void userApiGetUserMailing() throws IOException {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();
        UserMailingRepresentation  userMailing = keycloakUtils.getUserMailingUserApi("db0be0a5-e96e-40b6-b687-fdb12304b69a", "export-usermailing1", "test");
        Assert.assertNotNull(userMailing);
        Assert.assertEquals(userMailing.getUserId(), "1d4c3888-d6ea-4bd2-b50e-6d34f1345a05");
        Assert.assertEquals(userMailing.getMailingId(), "db0be0a5-e96e-40b6-b687-fdb12304b69a");
    }

    @Test
    public void userApiGetUserMailingUnauthorized() {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();
        try {
            //usermailing id belongs to user export-usermailing1 and not 2
            keycloakUtils.getUserMailingUserApi("db0be0a5-e96e-40b6-b687-fdb12304b69a", "export-usermailing1", "wrong");
            Assert.fail();
        } catch (IOException e){
            Assert.assertTrue(e.getMessage().startsWith("Error 401"));
        }
    }

    @Test
    public void userApiCreateUserMailing() throws IOException {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();

        UserMailingRepresentation rep = new UserMailingRepresentation();
        rep.setMailingId("1f57461d-ac02-497a-acfd-14d68c88f9bf");
        rep.setLanguage("nl");
        rep.setDelivery(1);
        int status = keycloakUtils.createUserMailingUserApi(rep, "user-createusermailing", "test");
        Assert.assertEquals(201, status);
    }

    @Test
    public void userApiCreateUserMailingMissingMailingId()  {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();

        UserMailingRepresentation rep = new UserMailingRepresentation();
        try {
            keycloakUtils.createUserMailingUserApi(rep, "user-createusermailing", "test");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertEquals("Error 400: {\"errorMessage\":\"No mailing id set for user mailing!\"}", e.getMessage());
        }

    }

    @Test
    public void userApiCreateUserMailingAlreadyExists() throws IOException {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();

        UserMailingRepresentation rep = new UserMailingRepresentation();
        rep.setMailingId("1f57461d-ac02-497a-acfd-14d68c88f9bf"); //CreateUserMailing
        rep.setLanguage("nl");
        rep.setDelivery(1);
        int status = keycloakUtils.createUserMailingUserApi(rep, "user-createusermailing-exists", "test");
        Assert.assertEquals(201, status);

        try {
            keycloakUtils.createUserMailingUserApi(rep, "user-createusermailing-exists", "test");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertEquals("Error 409: {\"errorMessage\":\"User mailing already exists for user 4fc46f48-2eec-4ff3-a86a-100ace11f92e and mailing 1f57461d-ac02-497a-acfd-14d68c88f9bf\"}", e.getMessage());
        }
    }


    @Test
    public void userApiCreateUserMailingInvalidLanguage() {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();

        UserMailingRepresentation rep = new UserMailingRepresentation();
        rep.setMailingId("1f57461d-ac02-497a-acfd-14d68c88f9bf"); //CreateUserMailing
        rep.setLanguage("en");
        rep.setDelivery(1);
        try {
            keycloakUtils.createUserMailingUserApi(rep, "user-createusermailing", "test");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertEquals("Error 400: {\"errorMessage\":\"Invalid language en! Expected one of [nl]\"}", e.getMessage());
        }
    }

    @Test
    public void userApiCreateUserMailingInvalidDelivery() {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();

        UserMailingRepresentation rep = new UserMailingRepresentation();
        rep.setMailingId("1f57461d-ac02-497a-acfd-14d68c88f9bf");
        rep.setLanguage("nl");
        rep.setDelivery(0);
        try {
            keycloakUtils.createUserMailingUserApi(rep, "user-createusermailing", "test");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertEquals("Error 400: {\"errorMessage\":\"Invalid delivery e-mail! Expected post\"}", e.getMessage());
        }
    }

    @Test
    public void userApiCreateUserMailingUnauthorized() {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();

        UserMailingRepresentation rep = new UserMailingRepresentation();
        rep.setMailingId("1f57461d-ac02-497a-acfd-14d68c88f9bf");
        rep.setLanguage("nl");
        rep.setDelivery(1);
        try {
            keycloakUtils.createUserMailingUserApi(rep, "user-createusermailing", "wrong");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error 401"));
        }
    }

    @Test
    public void userApiUpdateMailing() throws IOException {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();

        UserMailingRepresentation rep = new UserMailingRepresentation();
        rep.setMailingId("8f7f7566-865b-44f5-8fff-c2d510884d55");
        rep.setLanguage("en");
        rep.setDeliveryTxt(Mailing.deliveries.get(0)); //e-mail
        int status = keycloakUtils.createUserMailingUserApi(rep, "user-updateusermailing", "test");
        Assert.assertEquals(201, status);
        UserMailingRepresentation created = keycloakUtils.getUserMailingUserApi("8f7f7566-865b-44f5-8fff-c2d510884d55", "user-updateusermailing", "test");
        Assert.assertEquals("en", created.getLanguage());
        Assert.assertEquals(0, created.getDelivery());

        created.setDeliveryTxt(Mailing.deliveries.get(1)); //post
        created.setLanguage("nl");
        status = keycloakUtils.updateUserMailingUserApi(created, "user-updateusermailing", "test");
        Assert.assertEquals(200, status);

        UserMailingRepresentation updated = keycloakUtils.getUserMailingUserApi("8f7f7566-865b-44f5-8fff-c2d510884d55", "user-updateusermailing", "test");
        Assert.assertEquals("nl", updated.getLanguage());
        Assert.assertEquals(1, updated.getDelivery());

    }

    @Test
    /**
     * Get existing mailing and upload this using a different user account
     */
    public void userApiUpdateMailingUnauthorized() throws IOException {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();
        UserMailingRepresentation existing = keycloakUtils.getUserMailingUserApi("db0be0a5-e96e-40b6-b687-fdb12304b69a", "export-usermailing1", "test");

        try {
            keycloakUtils.updateUserMailingUserApi(existing, "export-usermailing2", "test");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error 403"));
        }
    }

    @Test
    public void userApiDeleteUserMailing() throws IOException {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();
        int response = keycloakUtils.deleteUserMailingUserApi("cb36038e-f139-43e0-a51b-874115fce148", "user-deletemailing", "test");
        Assert.assertEquals(200, response);
    }

    @Test
    public void userApiDeleteUserMailingUnauthorized() {
        KeycloakUtilsImpl keycloakUtils = KeycloakTestServer.getUserKeycloakUtils();
        try {
            keycloakUtils.deleteUserMailingUserApi("cb36038e-f139-43e0-a51b-874115fce148", "user-deletemailing", "wrong");
            Assert.fail();
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().startsWith("Error 401"));
        }
    }

    @Test
    /**
     * Test that when a Mailing is deleted, all linked user-mailings are also deleted
     */
    public void adminApiCascadeDeleteOfMailing(){
        //todo
    }

    @Test
    /**
     * Test that when a User is deleted, all linked user-mailings are also deleted
     */
    public void adminApiCascadeDeleteOfUser(){
        //todo
    }

}
