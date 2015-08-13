package ru.trilan.socialok;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import android.content.Context;
import android.widget.Toast;
import android.util.Log;
import android.os.AsyncTask;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

public class SocialOk extends CordovaPlugin {
    private static final String TAG = "SocialOk";
    private static final String ACTION_INIT = "initSocialOk";
    private static final String ACTION_LOGIN = "login";
    private static final String ACTION_SHARE = "share";
    private static final String ACTION_FRIENDS_GET = "friendsGet";
    private static final String ACTION_FRIENDS_GET_ONLINE = "friendsGetOnline";
    private static final String ACTION_STREAM_PUBLISH = "streamPublish";
    private static final String ACTION_USERS_GET_INFO = "usersGetInfo";
    private static final String ACTION_CALL_API_METHOD = "callApiMethod";
    private Odnoklassniki odnoklassnikiObject;
    private CallbackContext _callbackContext;

    /**
     * Gets the application context from cordova's main activity.
     * @return the application context
     */
    private Context getApplicationContext() {
        return this.webView.getContext();
    }
	
    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        this._callbackContext = callbackContext;
        if(ACTION_INIT.equals(action)) {
            return init(args.getString(0), args.getString(1), args.getString(2));
        } else if (ACTION_LOGIN.equals(action)) {
        } else if (ACTION_SHARE.equals(action)) {
            return shareOrLogin(args.getString(0), args.getString(1));
        } else if (ACTION_FRIENDS_GET.equals(action)) {
        } else if (ACTION_FRIENDS_GET_ONLINE.equals(action)) {
        } else if (ACTION_STREAM_PUBLISH.equals(action)) {
        } else if (ACTION_USERS_GET_INFO.equals(action)) {
        } else if (ACTION_CALL_API_METHOD.equals(action)) {
            String method = args.getString(0);
            JSONObject params = args.getJSONObject(1);
            return callApiMethod(method, JsonHelper.toMap(params), callbackContext);
        }
        Log.e(TAG, "Unknown action: "+action);
        _callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Unimplemented method: "+action));
        _callbackContext.error("Unimplemented method: "+action);
        return true;
    }

    private boolean init(String appId, String secret, String key)
    {
        odnoklassnikiObject = Odnoklassniki.createInstance(webView.getContext(), appId, secret, key);
        _callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        _callbackContext.success();
        return true;
    }

    private boolean shareOrLogin(final String url, final String comment)
    {
        //определяем callback на операции с получением токена
        odnoklassnikiObject.setTokenRequestListener(new OkTokenRequestListener() {
                @Override
                public void onSuccess(String token) {
                    Log.i(TAG, "Odnoklassniki accessToken = " + token);
                    if (token == null)
                        Toast.makeText(webView.getContext(), "Не удалось авторизоваться в приложении через \"Одноклассников\"."
                                       + "\nОшибка на сервере \"Одноклассников\".", Toast.LENGTH_LONG).show();
                    else
                        share(url, comment);
                }

                @Override
                public void onCancel() {
                    Log.i(TAG, "Auth cancel");
                    Toast.makeText(webView.getContext(), "Не удалось авторизоваться в приложении через \"Одноклассников\"."
                                   + "\nПроверьте соединение с Интернетом.", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError() {
                    Log.i(TAG, "Auth error");
                    Toast.makeText(webView.getContext(), "Ошибка во время авторизации в приложении через \"Одноклассников\".",
                                   Toast.LENGTH_LONG).show();
                }
            });
        //вызываем запрос авторизации. После OAuth будет вызван callback, определенный для объекта
        odnoklassnikiObject.requestAuthorization(webView.getContext(), false, OkScope.VALUABLE_ACCESS);
        return true;
    }

    private boolean share(final String url, final String comment)
    {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("linkUrl", url);
        params.put("comment", comment);
        new AsyncTask<String, Void, String>() {
            @Override protected String doInBackground(String... args) {
                try {
                    return odnoklassnikiObject.request("share.addLink", params, "get");
                } catch (IOException e) {
                    e.printStackTrace();
                    _callbackContext.error("Error");
                }
                return null;
            }
            @Override protected void onPostExecute(String result) {
                Log.i(TAG, "OK share result" + result);
                _callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                _callbackContext.success();
            }
        }.execute();
        return true;
    }

    private boolean callApiMethod(final String method, final Map<String, String> params, final CallbackContext context) 
    {
        new AsyncTask<String, Void, String>() {
            @Override protected String doInBackground(String... args) {
                try {
                    return odnoklassnikiObject.request(method, params, "post");
                } catch (IOException e) {
                    context.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.toString()));
                    context.error("Error");
                }
                return null;
            }
            @Override protected void onPostExecute(String result) {
                context.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                context.success();
            }
        }.execute();
        return true;
    }
}
