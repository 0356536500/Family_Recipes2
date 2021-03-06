package com.ronginat.family_recipes.layout.cognito;

import android.content.Context;
import android.graphics.Color;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.model.AttributeType;
import com.ronginat.family_recipes.MyApplication;
import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.layout.Constants;
import com.ronginat.family_recipes.utils.logic.CrashLogger;
import com.ronginat.family_recipes.utils.logic.SharedPreferencesHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
@SuppressWarnings("UnusedDeclaration")
public class AppHelper {
    private static final String TAG = "AppHelper";
    // App settings

    private static List<String> attributeDisplaySeq;
    private static Map<String, String> signUpFieldsC2O;
    private static Map<String, String> signUpFieldsO2C;

    private static AppHelper appHelper;
    private static CognitoCachingCredentialsProvider credentialsProvider;
    private static CognitoUserPool userPool;
    private static String user;
    private static CognitoDevice newDevice;

    private static CognitoUserAttributes attributesChanged;
    private static List<AttributeType> attributesToDelete;

    private static List<ItemToDisplay> currDisplayedItems;
    private static  int itemCount;

    private static List<ItemToDisplay> trustedDevices;
    private static int trustedDevicesCount;
    private static List<CognitoDevice> deviceDetails;
    private static CognitoDevice thisDevice;
    private static boolean thisDeviceTrustState;

    private static List<ItemToDisplay> firstTimeLogInDetails;
    private static Map<String, String> firstTimeLogInUserAttributes;
    private static List<String> firstTimeLogInRequiredAttributes;
    private static int firstTimeLogInItemsCount;
    private static Map<String, String> firstTimeLogInUpDatedAttributes;
    private static String firstTimeLoginNewPassword;

    private static List<ItemToDisplay> mfaOptions;
    private static List<String> mfaAllOptionsCode;

    // Change the next three lines of code to run this demo on your user pool

    /**
     * Add your pool id here
     */
    private static final String userPoolId = Constants.COGNITO_POOL_ID;

    /**
     * Add you app id
     */
    private static final String clientId = Constants.COGNITO_CLIENT_ID;

    /**
     * App secret associated with your app id - if the App id does not have an associated App secret,
     * set the App secret to null.
     * e.g. clientSecret = null;
     */
    private static final String clientSecret = Constants.COGNITO_CLIENT_SECRET;

    /**
     * Set Your User Pools region.
     * e.g. if your user pools are in US East (N Virginia) then set cognitoRegion = Regions.US_EAST_1.
     */
    private static final Regions cognitoRegion = Constants.COGNITO_REGION;

    // User details from the service
    public static PublishSubject<CognitoUserSession> currSessionObservable = PublishSubject.create();
    private static CognitoUserSession currSession;
    private static CognitoUserDetails userDetails;

    // User details to display - they are the current values, including any local modification
    private static boolean phoneVerified;
    private static boolean emailVerified;

    private static boolean phoneAvailable;
    private static boolean emailAvailable;

    private static Set<String> currUserAttributes;

    public static void init(Context context) {
        setData();

        if (appHelper != null && userPool != null) {
            return;
        }

        if (appHelper == null) {
            appHelper = new AppHelper();
        }

        if (userPool == null) {

            // Create a user pool with default ClientConfiguration
            userPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, cognitoRegion);

            // This will also work
            /*
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            AmazonCognitoIdentityProvider cipClient = new AmazonCognitoIdentityProviderClient(new AnonymousAWSCredentials(), clientConfiguration);
            cipClient.setRegion(Region.getRegion(cognitoRegion));
            userPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, cipClient);
            */


        }

        phoneVerified = false;
        phoneAvailable = false;
        emailVerified = false;
        emailAvailable = false;

        currUserAttributes = new HashSet<>();
        currDisplayedItems = new ArrayList<>();
        trustedDevices = new ArrayList<>();
        firstTimeLogInDetails = new ArrayList<>();
        firstTimeLogInUpDatedAttributes= new HashMap<>();

        newDevice = null;
        thisDevice = null;
        thisDeviceTrustState = false;

        mfaOptions = new ArrayList<>();
    }

    public static CognitoUserPool getPool() {
        return userPool;
    }

    public static Map<String, String> getSignUpFieldsC2O() {
        return signUpFieldsC2O;
    }

    public static Map<String, String> getSignUpFieldsO2C() {
        return signUpFieldsO2C;
    }

    public static List<String> getAttributeDisplaySeq() {
        return attributeDisplaySeq;
    }

    public static void setCurrSession(CognitoUserSession session) {
        currSession = session;
        currSessionObservable.onNext(session);
    }

    public static CognitoUserSession getCurrSession() {
        return currSession;
    }

    public static String getAccessToken() {
        /*if (getCurrSession() == null)
            return null;*/
        Date date = new Date();
        //date.setTime(date.getTime() + TimeUnit.MINUTES.toMillis(5));
        if (getCurrSession() == null || getCurrSession().getAccessToken().getExpiration().before(date)) {
            setUserSessionBackground(MyApplication.getContext());
            return null;
        }
        return getCurrSession().getAccessToken().getJWTToken();
    }

    /**
     * initiating user session for valid access token
     * subscribe to {@link AppHelper#currSessionObservable} for updates on {@link AppHelper#currSession}
     * @param context application context
     */
    private static void setUserSessionBackground(Context context) {
        if (getPool() == null)
            return;
        CognitoUser user = getPool().getCurrentUser();
        String username = user.getUserId();
        //user saved in cache
        if(username != null) {
            AppHelper.setUser(username);
            user.getSessionInBackground(getAuthenticationHandler(context, true));
        }
        //re-signing
        else {
            signInUser(context);
        }
    }

    /**
     * retrieve CognitoUser details (email, name, etc.)
     */
    public static void setUserDetailsBackground(Context context) {
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).getDetailsInBackground(getDetailsHandler(context));
    }

    public static void signOutUser() {
        getPool().getUser(user).signOut();
    }

    private static void signInUser(Context context) {
        String username = SharedPreferencesHandler.getString(context, Constants.USERNAME);
        String password = SharedPreferencesHandler.getString(context, Constants.PASSWORD);
        if(username != null && password != null) {
            AppHelper.setUser(username);

            AppHelper.getPool().getUser(username).getSessionInBackground(getAuthenticationHandler(context, false));
        }
        /*else
            launchLogin();*/
    }

    private static AuthenticationHandler getAuthenticationHandler(Context context, boolean retryOnError) {
        return new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
                //Log.d(TAG, " -- Auth Success");
                AppHelper.setCurrSession(cognitoUserSession);
                AppHelper.setUserDetailsBackground(context);
                AppHelper.newDevice(device);
                //Log.e(TAG, "IDToken: " + cognitoUserSession.getIdToken().getJWTToken());
                //Log.e(TAG, "AccessToken: " + cognitoUserSession.getAccessToken().getJWTToken());

                AppHelper.setIdentityProvider(context, cognitoUserSession);
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
                Locale.setDefault(Locale.US);
                getUserAuthentication(context, authenticationContinuation, username);
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
                //mfaAuth(multiFactorAuthenticationContinuation);
            }

            @Override
            public void onFailure(Exception e) {
                CrashLogger.logException(e);
                if (retryOnError)
                    signInUser(context);
                else
                    currSessionObservable.onError(e);
                //Log.e(TAG, "Sign-in failed, " + AppHelper.formatException(e));
            }

            /**
             * For Custom authentication challenge, implement your logic to present challenge to the
             * user and pass the user's responses to the continuation.
             */
            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
                /*if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
                    Log.i(TAG, "NEW PASSWORD REQUIRED");
                } else if ("SELECT_MFA_TYPE".equals(continuation.getChallengeName())) {
                    Log.i(TAG, "SELECT MFA TYPE");
                }*/
            }
        };
    }

    private static GetDetailsHandler getDetailsHandler(Context context) {
        return new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                AppHelper.setUserDetails(context, cognitoUserDetails);
            }

            @Override
            public void onFailure(Exception exception) {
                CrashLogger.logException(exception);
                //Log.e(TAG, exception.getMessage(), exception);
            }
        };
    }

    private static void getUserAuthentication(Context context, AuthenticationContinuation continuation, String username) {
        if(username != null) {
            AppHelper.setUser(username);
        }
        String password = SharedPreferencesHandler.getString(context, Constants.PASSWORD);
        if(password == null) {
            //Log.e(TAG, "enter password1");
            return;
        }

        AuthenticationDetails authenticationDetails = new AuthenticationDetails(username, password, null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }

    private static void setUserDetails(Context context, CognitoUserDetails details) {
        userDetails = details;
        if (details != null)
            refreshWithSync(context);
    }

    public static CognitoUserDetails getUserDetails() {
        return userDetails;
    }

    public static String getCurrUser() {
        return user;
    }

    public static void setUser(String newUser) {
        user = newUser;
    }

    public static CognitoCachingCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public static void setIdentityProvider(Context context, CognitoUserSession cognitoUserSession) {
        // Create a credentials provider, or use the existing provider.
        credentialsProvider = new CognitoCachingCredentialsProvider
                (context, Constants.COGNITO_IDENTITY_POOL_ID, Constants.COGNITO_REGION);

        // Set up as a credentials provider.
        Map<String, String> logins = new HashMap<>();
        logins.put(Constants.COGNITO_IDENTITY_LOGIN, cognitoUserSession.getIdToken().getJWTToken());
        credentialsProvider.setLogins(logins);
    }

    public static boolean isPhoneVerified() {
        return phoneVerified;
    }

    public static boolean isEmailVerified() {
        return emailVerified;
    }

    public static boolean isPhoneAvailable() {
        return phoneAvailable;
    }

    public static boolean isEmailAvailable() {
        return emailAvailable;
    }

    public static void setPhoneVerified(boolean phoneVerif) {
        phoneVerified = phoneVerif;
    }

    public static void setEmailVerified(boolean emailVerif) {
        emailVerified = emailVerif;
    }

    public static void setPhoneAvailable(boolean phoneAvail) {
        phoneAvailable = phoneAvail;
    }

    public static void setEmailAvailable(boolean emailAvail) {
        emailAvailable = emailAvail;
    }

    public static void clearCurrUserAttributes() {
        currUserAttributes.clear();
    }

    public static void addCurrUserattribute(String attribute) {
        currUserAttributes.add(attribute);
    }

    public static List<String> getNewAvailableOptions() {
        List<String> newOption = new ArrayList<>();
        for(String attribute : attributeDisplaySeq) {
            if(!(currUserAttributes.contains(attribute))) {
                newOption.add(attribute);
            }
        }
        return  newOption;
    }

    public static String formatException(Exception exception) {
        String formattedString = "Internal Error";
        CrashLogger.e(TAG, " -- Error: " + exception.toString());
        //Log.getStackTraceString(exception);

        String temp = exception.getMessage();

        if(temp != null && temp.length() > 0) {
            formattedString = temp.split("\\(")[0];
            if(formattedString != null && formattedString.length() > 0) {
                return formattedString;
            }
        }

        return formattedString;
    }

    public  static  int getItemCount() {
        return itemCount;
    }

    public static int getDevicesCount() {
        return trustedDevicesCount;
    }

    public static int getFirstTimeLogInItemsCount() {
        return  firstTimeLogInItemsCount;
    }

    public  static ItemToDisplay getItemForDisplay(int position) {
        return  currDisplayedItems.get(position);
    }

    public static ItemToDisplay getDeviceForDisplay(int position) {
        if (position >= trustedDevices.size()) {
            return new ItemToDisplay(" ", " ", " ", Color.BLACK, Color.DKGRAY, Color.parseColor("#37A51C"), 0, null);
        }
        return trustedDevices.get(position);
    }

    public static ItemToDisplay getUserAttributeForFirstLogInCheck(int position) {
        return firstTimeLogInDetails.get(position);
    }

    public static void setUserAttributeForDisplayFirstLogIn(Map<String, String> currAttributes, List<String> requiredAttributes) {
        firstTimeLogInUserAttributes = currAttributes;
        firstTimeLogInRequiredAttributes = requiredAttributes;
        firstTimeLogInUpDatedAttributes = new HashMap<>();
        refreshDisplayItemsForFirstTimeLogin();
    }

    public static void setUserAttributeForFirstTimeLogin(String attributeName, String attributeValue) {
        if (firstTimeLogInUserAttributes ==  null) {
            firstTimeLogInUserAttributes = new HashMap<>();
        }
        firstTimeLogInUserAttributes.put(attributeName, attributeValue);
        firstTimeLogInUpDatedAttributes.put(attributeName, attributeValue);
        refreshDisplayItemsForFirstTimeLogin();
    }

    public static boolean isUserAttributeForFirstTimeLoginContainsAttribute(String attributeName) {
        if (firstTimeLogInUserAttributes != null) {
            return firstTimeLogInUserAttributes.get(attributeName) != null;
        }
        return false;
    }

    public static Map<String, String> getUserAttributesForFirstTimeLogin() {
        return firstTimeLogInUpDatedAttributes;
    }

    public static void setPasswordForFirstTimeLogin(String password) {
        firstTimeLoginNewPassword = password;
    }

    public static String getPasswordForFirstTimeLogin() {
        return firstTimeLoginNewPassword;
    }

    private static void refreshDisplayItemsForFirstTimeLogin() {
        firstTimeLogInItemsCount = 0;
        firstTimeLogInDetails = new ArrayList<>();

        for(Map.Entry<String, String> attr: firstTimeLogInUserAttributes.entrySet()) {
            if ("phone_number_verified".equals(attr.getKey()) || "email_verified".equals(attr.getKey())) {
                continue;
            }
            String message = "";
            if ((firstTimeLogInRequiredAttributes != null) && (firstTimeLogInRequiredAttributes.contains(attr.getKey()))) {
                message = "Required";
            }
            ItemToDisplay item = new ItemToDisplay(attr.getKey(), attr.getValue(), message, Color.BLACK, Color.DKGRAY, Color.parseColor("#329AD6"), 0, null);
            firstTimeLogInDetails.add(item);
            firstTimeLogInRequiredAttributes.size();
            firstTimeLogInItemsCount++;
        }

        for (String attr: firstTimeLogInRequiredAttributes) {
            if (!firstTimeLogInUserAttributes.containsKey(attr)) {
                ItemToDisplay item = new ItemToDisplay(attr, "", "Required", Color.BLACK, Color.DKGRAY, Color.parseColor("#329AD6"), 0, null);
                firstTimeLogInDetails.add(item);
                firstTimeLogInItemsCount++;
            }
        }
    }

    public static void newDevice(CognitoDevice device) {
        newDevice = device;
    }

    public static void setDevicesForDisplay(List<CognitoDevice> devicesList) {
        trustedDevicesCount = 0;
        thisDeviceTrustState = false;
        deviceDetails = devicesList;
        trustedDevices = new ArrayList<>();
        for(CognitoDevice device: devicesList) {
            if (thisDevice != null && thisDevice.getDeviceKey().equals(device.getDeviceKey())) {
                thisDeviceTrustState = true;
            } else {
                ItemToDisplay item = new ItemToDisplay("", device.getDeviceName(), device.getCreateDate().toString(), Color.BLACK, Color.DKGRAY, Color.parseColor("#329AD6"), 0, null);
                item.setDataDrawable("checked");
                trustedDevices.add(item);
                trustedDevicesCount++;
            }
        }
    }

    public static CognitoDevice getDeviceDetail(int position) {
        if (position <= trustedDevicesCount) {
            return deviceDetails.get(position);
        } else {
            return null;
        }
    }

    public static void setMfaOptionsForDisplay(List<String> options, Map<String, String> parameters) {
        mfaAllOptionsCode = options;
        mfaOptions = new ArrayList<>();
        String textToDisplay = "";
        for (String option: options) {
            if ("SMS_MFA".equals(option)) {
                textToDisplay = "Send SMS";
                if (parameters.containsKey("CODE_DELIVERY_DESTINATION")) {
                    textToDisplay = textToDisplay + " to "+ parameters.get("CODE_DELIVERY_DESTINATION");
                }
            } else if ("SOFTWARE_TOKEN_MFA".equals(option)) {
                textToDisplay = "Use TOTP";
                if (parameters.containsKey("FRIENDLY_DEVICE_NAME")) {
                    textToDisplay = textToDisplay + ": " + parameters.get("FRIENDLY_DEVICE_NAME");
                }
            }
            ItemToDisplay item = new ItemToDisplay("", textToDisplay, "", Color.BLACK, Color.DKGRAY, Color.parseColor("#329AD6"), 0, null);
            mfaOptions.add(item);
            textToDisplay = "Unsupported MFA";
        }
    }

    public static List<String> getAllMfaOptions() {
        return mfaAllOptionsCode;
    }

    public static String getMfaOptionCode(int position) {
        return mfaAllOptionsCode.get(position);
    }

    public static ItemToDisplay getMfaOptionForDisplay(int position) {
        if (position >= mfaOptions.size()) {
            return new ItemToDisplay(" ", " ", " ", Color.BLACK, Color.DKGRAY, Color.parseColor("#37A51C"), 0, null);
        }
        return mfaOptions.get(position);
    }

    public static int getMfaOptionsCount() {
        return mfaOptions.size();
    }

    //public static

    public static CognitoDevice getNewDevice() {
        return newDevice;
    }

    public static CognitoDevice getThisDevice() {
        return thisDevice;
    }

    public static void setThisDevice(CognitoDevice device) {
        thisDevice = device;
    }

    public static boolean getThisDeviceTrustState() {
        return thisDeviceTrustState;
    }

    private static void setData() {
        // Set attribute display sequence
        attributeDisplaySeq = new ArrayList<>();
        attributeDisplaySeq.add("given_name");
        attributeDisplaySeq.add("middle_name");
        attributeDisplaySeq.add("family_name");
        attributeDisplaySeq.add("nickname");
        attributeDisplaySeq.add("phone_number");
        attributeDisplaySeq.add("email");

        signUpFieldsC2O = new HashMap<>();
        signUpFieldsC2O.put("Given name", "given_name");
        signUpFieldsC2O.put("Family name", "family_name");
        signUpFieldsC2O.put("Nick name", "nickname");
        signUpFieldsC2O.put("Phone number", "phone_number");
        signUpFieldsC2O.put("Phone number verified", "phone_number_verified");
        signUpFieldsC2O.put("Email verified", "email_verified");
        signUpFieldsC2O.put("Email","email");
        signUpFieldsC2O.put("Middle name","middle_name");

        signUpFieldsO2C = new HashMap<>();
        signUpFieldsO2C.put("given_name", "Given name");
        signUpFieldsO2C.put("family_name", "Family name");
        signUpFieldsO2C.put("nickname", "Nick name");
        signUpFieldsO2C.put("phone_number", "Phone number");
        signUpFieldsO2C.put("phone_number_verified", "Phone number verified");
        signUpFieldsO2C.put("email_verified", "Email verified");
        signUpFieldsO2C.put("email", "Email");
        signUpFieldsO2C.put("middle_name", "Middle name");

    }

    private static void refreshWithSync(Context context) {
        // This will refresh the current items to display list with the attributes fetched from service
        List<String> tempKeys = new ArrayList<>();
        List<String> tempValues = new ArrayList<>();

        emailVerified = false;
        phoneVerified = false;

        emailAvailable = false;
        phoneAvailable = false;

        currDisplayedItems = new ArrayList<>();
        currUserAttributes.clear();
        itemCount = 0;

        for(Map.Entry<String, String> attr: userDetails.getAttributes().getAttributes().entrySet()) {

            tempKeys.add(attr.getKey());
            tempValues.add(attr.getValue());

            if(attr.getKey().contains("email_verified")) {
                emailVerified = attr.getValue().contains("true");
            }
            else if(attr.getKey().contains("phone_number_verified")) {
                phoneVerified = attr.getValue().contains("true");
            }

            switch (attr.getKey()) {
                case "email":
                    emailAvailable = true;
                    SharedPreferencesHandler.writeString(context, attr.getKey(), attr.getValue());
                    break;
                case Constants.FIRESTORE_DISPLAYED_NAME: // name
                    SharedPreferencesHandler.writeString(context, context.getString(R.string.preference_key_preferred_name), attr.getValue());
                    break;
                case "phone_number":
                    phoneAvailable = true;
                    break;
            }
        }

        // Arrange the input attributes per the display sequence
        Set<String> keySet = new HashSet<>(tempKeys);
        for(String det: attributeDisplaySeq) {
            if(keySet.contains(det)) {
                // Adding items to display list in the required sequence

                ItemToDisplay item = new ItemToDisplay(signUpFieldsO2C.get(det), tempValues.get(tempKeys.indexOf(det)), "",
                        Color.BLACK, Color.DKGRAY, Color.parseColor("#37A51C"),
                        0, null);

                if(det.contains("email")) {
                    if(emailVerified) {
                        item.setDataDrawable("checked");
                        item.setMessageText("Email verified");
                    }
                    else {
                        item.setDataDrawable("not_checked");
                        item.setMessageText("Email not verified");
                        item.setMessageColor(Color.parseColor("#E94700"));
                    }
                }

                if(det.contains("phone_number")) {
                    if(phoneVerified) {
                        item.setDataDrawable("checked");
                        item.setMessageText("Phone number verified");
                    }
                    else {
                        item.setDataDrawable("not_checked");
                        item.setMessageText("Phone number not verified");
                        item.setMessageColor(Color.parseColor("#E94700"));
                    }
                }
                
                currDisplayedItems.add(item);
                currUserAttributes.add(det);
                itemCount++;
            }
        }
    }

    // Notify when cognito attribute had changed or got error while modifying it
    public static PublishSubject<Boolean> updateAttributeSubject = PublishSubject.create();

    public static void modifyAttribute(Context context, String attributeName, String attributeValue) {
        if (attributeName == null || attributeValue == null)
            return;
        CognitoUserAttributes updatedUserAttributes = new CognitoUserAttributes();
        updatedUserAttributes.addAttribute(attributeName, attributeValue);
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).updateAttributesInBackground(updatedUserAttributes, getUpdateHandler(context));
    }

    // Callback handler
    private static UpdateAttributesHandler getUpdateHandler(Context context) {
        return new UpdateAttributesHandler() {
            @Override
            public void onSuccess(List<CognitoUserCodeDeliveryDetails> attributesVerificationList) {
                // Update successful
                if (attributesVerificationList.size() > 0) {
                    //Log.e(TAG, "The updated attributes has to be verified");
                    updateAttributeSubject.onNext(false);
                } else {
                    setUserDetailsBackground(context);
                    // give the background process time to update - 300 ms
                    Completable.timer(300, TimeUnit.MILLISECONDS, Schedulers.io())
                            .observeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    //Log.e(TAG, "attribute updated");
                                    updateAttributeSubject.onNext(true);
                                    dispose();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    // error
                                    e.printStackTrace();
                                }
                            });
                }
            }

            @Override
            public void onFailure(Exception exception) {
                // Update failed
                //Log.e(TAG, "Update failed, " + AppHelper.formatException(exception));
                CrashLogger.logException(exception);
                updateAttributeSubject.onNext(false);
            }
        };
    }

    private static void deleteAttribute(String attributeName) {

    }
}