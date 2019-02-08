package com.ayalus.exoplayer2example;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.util.TextUtils;


public final class SSPMediaDrmCallback implements MediaDrmCallback {
    int MAX_MANUAL_REDIRECTS = 5;
    private static SSPMediaDrmCallback sspMediaDrmCallback = null;
    private HttpDataSource.Factory dataSourceFactory;
    private String defaultLicenseUrl;
    private boolean forceDefaultLicenseUrl;
    private Map<String, String> keyRequestProperties;
    private static final String SSP_TENANT_ID = "JEA3LA5O";
    private static final String CONTENT_TYPE = "application/json";
    private Context context;
    byte[] payload = null;
    private static final String TAG = "SSPMediaDRMCallback";
    private String contentToken;

    public SSPMediaDrmCallback(Context context) {
        this.context = context;
    }

    public void setContentToken(String content_token) {
        contentToken = content_token;
    }

    public SSPMediaDrmCallback(String defaultLicenseUrl, HttpDataSource.Factory dataSourceFactory) {
        this(defaultLicenseUrl, false, dataSourceFactory);
    }

    public SSPMediaDrmCallback(String defaultLicenseUrl, boolean forceDefaultLicenseUrl,
                               HttpDataSource.Factory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        this.defaultLicenseUrl = defaultLicenseUrl;
        this.forceDefaultLicenseUrl = forceDefaultLicenseUrl;
        this.keyRequestProperties = new HashMap<>();
    }

    @Override
    public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest request) throws Exception {
        String url =
                request.getDefaultUrl() + "&signedRequest=" + Util.fromUtf8Bytes(request.getData());
        return executePost(dataSourceFactory, url, new byte[0], null);
    }


    /*public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest request, @Nullable String mediaProvidedLicenseServerUrl) throws Exception {
        byte[] responseByteData = null;
        HttpClient httpClient = new DefaultHttpClient();
        String postJsonData = "{\"challenge\":\"CAQ=\"}";
        HttpPost httpPost = new HttpPost(defaultLicenseUrl);

        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("nv-authorizations", contentToken);
        httpPost.addHeader("nv-tenant-id", SSP_TENANT_ID);

        StringEntity jsonParam = new StringEntity(postJsonData.toString());
        httpPost.setEntity(jsonParam);
        String licenseStrBase64Encoded = "";
        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity respEntity = response.getEntity();
            if (respEntity != null) {
                // EntityUtils to get the response content
                String content = EntityUtils.toString(respEntity);
                responseByteData = content.getBytes();
                responseByteData = parseResponse(responseByteData);
                Log.d(TAG, "Response Entity : " + content);
            }
        } catch (ClientProtocolException e) {
            // writing exception to log
            e.printStackTrace();
        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        }

        return responseByteData;
    }*/


    public byte[] parseResponse(byte[] xResponse) {
        byte[] payload = null;
        String licenseStrBase64Encoded = "";
        try {
            JSONObject responseJson = new JSONObject(new String(xResponse));
            licenseStrBase64Encoded = responseJson.getJSONArray("license").get(0).toString();
            Log.d(TAG, "licenseStrBase64Encoded : "+licenseStrBase64Encoded);
        } catch (JSONException jEx) {
            jEx.printStackTrace();
        }

        payload = Base64.decode(licenseStrBase64Encoded.getBytes(), Base64.NO_WRAP);
        return payload;
    }

    @Override
    public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest request, @Nullable String mediaProvidedLicenseServerUrl) throws Exception {
        String url = request.getDefaultUrl();
        String postJsonData = "{\"challenge\":\"CAQ=\"}";
        byte [] data = postJsonData.getBytes();
        byte response[], response1[];
        if (TextUtils.isEmpty(url)) {
            url = mediaProvidedLicenseServerUrl;
        }
        if (forceDefaultLicenseUrl || TextUtils.isEmpty(url)) {
            url = defaultLicenseUrl;
        }
        Map<String, String> requestProperties = new HashMap<>();
        // Add standard request properties for supported schemes.
        String contentType = "application/json";
        requestProperties.put("Content-Type", contentType);
        requestProperties.put("Accept", contentType);
        requestProperties.put("nv-authorizations", contentToken);
        Log.d(TAG, "token : "+contentToken);
        requestProperties.put("nv-tenant-id", SSP_TENANT_ID);
        // Add additional request properties.
        response = (executePost(dataSourceFactory, url, data, requestProperties));
        String res = new String(response);
        Log.d(TAG, "Response : "+res);
        response1 = parseResponse(response);
        return response;
    }


    private static byte[] executePost(HttpDataSource.Factory dataSourceFactory, String url,
                                      byte[] data, Map<String, String> requestProperties) throws IOException, JSONException {
        HttpDataSource dataSource = dataSourceFactory.createDataSource();
        byte [] response;
        if (requestProperties != null) {
            for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
                dataSource.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
            }
        }

        int manualRedirectCount = 0;
        while (true) {
            DataSpec dataSpec =
                    new DataSpec(
                            Uri.parse(url),
                            data,
                            /* absoluteStreamPosition= */ 0,
                            /* position= */ 0,
                            /* length= */ C.LENGTH_UNSET,
                            /* key= */ null,
                            DataSpec.FLAG_ALLOW_GZIP);
            DataSourceInputStream inputStream = new DataSourceInputStream(dataSource, dataSpec);
            try {
                response = Util.toByteArray(inputStream);
                return response;
            } catch (HttpDataSource.InvalidResponseCodeException e) {
                // For POST requests, the underlying network stack will not normally follow 307 or 308
                // redirects automatically. Do so manually here.
                boolean manuallyRedirect =
                        (e.responseCode == 307 || e.responseCode == 308)
                                && manualRedirectCount++ < 5;
                url = manuallyRedirect ? getRedirectUrl(e) : null;
                if (url == null) {
                    throw e;
                }
            } finally {
                Util.closeQuietly(inputStream);
            }
        }
    }

    private static String getRedirectUrl(HttpDataSource.InvalidResponseCodeException exception) {
        Map<String, List<String>> headerFields = exception.headerFields;
        if (headerFields != null) {
            List<String> locationHeaders = headerFields.get("Location");
            if (locationHeaders != null && !locationHeaders.isEmpty()) {
                return locationHeaders.get(0);
            }
        }
        return null;
    }

}
