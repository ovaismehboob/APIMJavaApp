package javaapp;

import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Hello world!
 */
public final class App {
        private App() {
        }

        /**
         * Says hello to the world.
         * 
         * @param args The arguments of the program.
         */
        public static void main(String[] args) throws Exception {
                String firstName = "First Name";
                String lastName = "Last Name";
                String email = "EmailAddress@email.com";
                String clientId = "b88f2555-4b8a-464e-9940-5102df0a21a1";
                String authority = "https://login.microsoftonline.com/67c0d2af-96b4-4c3d-b701-6d9da42e80a1";
                String authUserEmail = "authuser@yourtenantname.onmicrosoft.com";
                String authUserPass = "123456789";
                String azureSubscriptionId = "subscriptionid";
                String resourceGroupName = "apigateway";
                String apiGatewayName = "yourapigatewayname";
                String userUID = "000000001";
                String jsonBody = "{ \"properties\": { \"firstName\": \"" + firstName + "\", \"" + lastName
                                + "\": \"usercode\", \"email\": \"" + email + "\", \"confirmation\": \"signup\"  } }";

                PublicClientApplication pca = PublicClientApplication.builder(clientId).authority(authority).build();

                Set<String> scp = new HashSet<String>();
                scp.add("https://management.azure.com/.default");

                IAuthenticationResult result = acquireTokenUsernamePassword(pca, scp, authUserEmail, authUserPass);
                System.out.println("Account username: " + result.account().username());
                System.out.println("Access token:     " + result.accessToken());
                // System.out.println("Id token: " + result.idToken());

                URL obj = new URL("https://management.azure.com/subscriptions/" + azureSubscriptionId
                                + "/resourceGroups/" + resourceGroupName + "/providers/Microsoft.ApiManagement/service/"
                                + apiGatewayName + "/users/" + userUID + "?api-version=2019-12-01");
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("PUT");
                con.setRequestProperty("Authorization", "Bearer " + result.accessToken());
                con.setRequestProperty("Content-Type", "application/json");
                // For POST only - START
                con.setDoOutput(true);
                // For POST only - END

                try (OutputStream os = con.getOutputStream()) {
                        byte[] input = jsonBody.getBytes("utf-8");
                        os.write(input, 0, input.length);
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                        }
                        System.out.println(response.toString());
                }
        }

        private static IAuthenticationResult acquireTokenUsernamePassword(PublicClientApplication pca,
                        Set<String> scope, String username, String password) {
                IAuthenticationResult result;
                UserNamePasswordParameters parameters = UserNamePasswordParameters
                                .builder(scope, username, password.toCharArray()).build();
                // Try to acquire a token via username/password. If successful, you should see
                // the token and account information printed out to console
                result = pca.acquireToken(parameters).join();
                System.out.println("==username/password flow succeeded");

                return result;
        }

}
