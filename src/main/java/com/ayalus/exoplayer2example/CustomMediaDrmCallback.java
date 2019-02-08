package com.ayalus.exoplayer2example;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ayalus.exoplayer2example.Networking.ApiInterface;
import com.ayalus.exoplayer2example.Networking.LicenseClient;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.util.TextUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomMediaDrmCallback implements MediaDrmCallback {
    private static final int MAX_MANUAL_REDIRECTS = 5;

    private final HttpDataSource.Factory dataSourceFactory;
    private final String defaultLicenseUrl;
    private final boolean forceDefaultLicenseUrl;
    private final Map<String, String> keyRequestProperties;

    public void setResponse(byte[] response) {
        Response = response;
    }

    byte [] Response;
    public void setContentTokenVal(String contentTokenVal) {
        ContentTokenVal = contentTokenVal;
    }

    private String ContentTokenVal;
    /**
     * @param defaultLicenseUrl The default license URL. Used for key requests that do not specify
     *     their own license URL.
     * @param dataSourceFactory A factory from which to obtain {@link HttpDataSource} instances.
     */
    public CustomMediaDrmCallback(String defaultLicenseUrl, HttpDataSource.Factory dataSourceFactory) {
        this(defaultLicenseUrl, false, dataSourceFactory);
    }

    /**
     * @param defaultLicenseUrl The default license URL. Used for key requests that do not specify
     *     their own license URL, or for all key requests if {@code forceDefaultLicenseUrl} is
     *     set to true.
     * @param forceDefaultLicenseUrl Whether to use {@code defaultLicenseUrl} for key requests that
     *     include their own license URL.
     * @param dataSourceFactory A factory from which to obtain {@link HttpDataSource} instances.
     */
    public CustomMediaDrmCallback(String defaultLicenseUrl, boolean forceDefaultLicenseUrl,
                                HttpDataSource.Factory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        this.defaultLicenseUrl = defaultLicenseUrl;
        this.forceDefaultLicenseUrl = forceDefaultLicenseUrl;
        this.keyRequestProperties = new HashMap<>();
    }

    /**
     * Sets a header for key requests made by the callback.
     *
     * @param name The name of the header field.
     * @param value The value of the field.
     */
    public void setKeyRequestProperty(String name, String value) {
        Assertions.checkNotNull(name);
        Assertions.checkNotNull(value);
        synchronized (keyRequestProperties) {
            keyRequestProperties.put(name, value);
        }
    }

    /**
     * Clears a header for key requests made by the callback.
     *
     * @param name The name of the header field.
     */
    public void clearKeyRequestProperty(String name) {
        Assertions.checkNotNull(name);
        synchronized (keyRequestProperties) {
            keyRequestProperties.remove(name);
        }
    }

    /**
     * Clears all headers for key requests made by the callback.
     */
    public void clearAllKeyRequestProperties() {
        synchronized (keyRequestProperties) {
            keyRequestProperties.clear();
        }
    }

    @Override
    public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest request) throws IOException {
        String url =
                request.getDefaultUrl() + "&signedRequest=" + Util.fromUtf8Bytes(request.getData());
        return executePost(dataSourceFactory, url, new byte[0], null);
    }

    @Override
    public byte[] executeKeyRequest(
            UUID uuid, ExoMediaDrm.KeyRequest request, @Nullable String mediaProvidedLicenseServerUrl)
            throws Exception {
        Log.d("Kapil","Request : "+request);
        //return Response;
        String postJsonData = "{\"challenge\":\"CAQ=\"}";
        byte [] data = postJsonData.getBytes();
        String url = request.getDefaultUrl();
        if (TextUtils.isEmpty(url)) {
            url = mediaProvidedLicenseServerUrl;
        }
        if (forceDefaultLicenseUrl || TextUtils.isEmpty(url)) {
            url = defaultLicenseUrl;
        }
        Map<String, String> requestProperties = new HashMap<>();
        synchronized (keyRequestProperties) {
            requestProperties.putAll(keyRequestProperties);
        }
        return executePost(dataSourceFactory, url, request.getData(), requestProperties);
    }

    private static byte[] executePost(HttpDataSource.Factory dataSourceFactory, String url,
                                      byte[] data, Map<String, String> requestProperties) throws IOException {
        HttpDataSource dataSource = dataSourceFactory.createDataSource();
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
                return Util.toByteArray(inputStream);
            } catch (HttpDataSource.InvalidResponseCodeException e) {
                // For POST requests, the underlying network stack will not normally follow 307 or 308
                // redirects automatically. Do so manually here.
                boolean manuallyRedirect =
                        (e.responseCode == 307 || e.responseCode == 308)
                                && manualRedirectCount++ < MAX_MANUAL_REDIRECTS;
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
