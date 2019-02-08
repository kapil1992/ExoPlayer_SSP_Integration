package com.ayalus.exoplayer2example;

import android.media.MediaDrm;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.widget.TextView;

import com.ayalus.exoplayer2example.Networking.ApiClient;
import com.ayalus.exoplayer2example.Networking.ApiInterface;
import com.ayalus.exoplayer2example.Networking.LicenseClient;
import com.ayalus.exoplayer2example.model.AccessTokenResponseModel;
import com.ayalus.exoplayer2example.model.ContentTokenResponseModel;
import com.ayalus.exoplayer2example.model.LicenseResponseModel;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    private static final int CALL_API = 1;
    ApiInterface apiService, licenseService;
    String Accesstoken, ContentToken;
    private PlayerView simpleExoPlayerView;
    private SimpleExoPlayer player = null;
    private TextView resolutionTextView;
    private SSPMediaDrmCallback callback;
    private CustomMediaDrmCallback httpMediaDrmCallback;
    private String userAgent;
    byte [] Response;
    private static final String SSP_TENANT_ID = "EUTY3FMG";
    Uri mp4VideoUri = Uri.parse("https://dash.ctl.test-venise.com/bpk-tv/orp02-ten999-ch002/dash/index.mpd?begin=20190207T140000");
    String LicenseUrl = "https://euty3fmg.anycast.nagra.com/EUTY3FMG/wvls/contentlicenseservice/v1/licenses";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resolutionTextView = new TextView(this);
        resolutionTextView = (TextView) findViewById(R.id.resolution_textView);
        userAgent = Util.getUserAgent(getApplicationContext(), "MyExoPlayer");
        apiService = ApiClient.getClient().create(ApiInterface.class);
        licenseService = LicenseClient.getClient().create(ApiInterface.class);
        getAccessToken();
    }

    private void createPlayer() {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //test

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent, bandwidthMeter);
        HttpDataSource.Factory httpdatasourcefactory = new DefaultHttpDataSourceFactory(userAgent);
        /*callback = new SSPMediaDrmCallback(LicenseUrl, httpdatasourcefactory);
        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("Content-Type", contentType);
        requestProperties.put("Accept", contentType);
        requestProperties.put("nv-authorizations", ContentToken);
        Log.d(TAG, "token : "+ContentToken);
        requestProperties.put("nv-tenant-id", SSP_TENANT_ID);
        String contentType = "application/octet-stream";*/
        httpMediaDrmCallback = new CustomMediaDrmCallback(LicenseUrl, httpdatasourcefactory);
//        httpMediaDrmCallback.setKeyRequestProperty("Accept", contentType);
//        httpMediaDrmCallback.setKeyRequestProperty("Content-Type", contentType);
        Log.d(TAG, "ContentToken : " + ContentToken);
        httpMediaDrmCallback.setKeyRequestProperty("nv-authorizations", ContentToken);
        httpMediaDrmCallback.setKeyRequestProperty("nv-tenant-id", SSP_TENANT_ID);
        DrmSessionManager drmSessionManager = null;
        try {
            drmSessionManager = new DefaultDrmSessionManager(
                    C.WIDEVINE_UUID,
                    FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID),
                    httpMediaDrmCallback,
                    null
            );
        } catch (UnsupportedDrmException e) {
            e.printStackTrace();
        }
        RenderersFactory renderersFactory = new DefaultRenderersFactory(this);
        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, drmSessionManager);
        simpleExoPlayerView = new SimpleExoPlayerView(this);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        int h = simpleExoPlayerView.getResources().getConfiguration().screenHeightDp;
        int w = simpleExoPlayerView.getResources().getConfiguration().screenWidthDp;
        simpleExoPlayerView.setUseController(false);//set to true or false to see controllers
        simpleExoPlayerView.requestFocus();
        // Bind the player to the view.
        simpleExoPlayerView.setPlayer(player);
        MediaSource videoSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory).createMediaSource(mp4VideoUri);
        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
        player.prepare(videoSource);
        startPlayer();
    }

    private void startPlayer() {
        player.setPlayWhenReady(true); //run file/link when ready to play.
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CALL_API) {
                getContentToken();
                handler.sendEmptyMessageDelayed(CALL_API, 5000);
                handler.removeMessages(CALL_API);
            }
        }
    };

    private void getAccessToken() {
        Call<AccessTokenResponseModel> accessTokenCall = apiService.getAccessToken(
                "password",
                "EUT",
                "EUT",
                "5c5a861f44c8b00001450f80",
                "nagra",
                "application/json"
        );
        accessTokenCall.enqueue(new Callback<AccessTokenResponseModel>() {
            @Override
            public void onResponse(Call<AccessTokenResponseModel> call, Response<AccessTokenResponseModel> response) {
                Log.d(TAG, "onResponse Access_token : " + response.body().getAccess_token());
                Log.d(TAG, "onResponse Call: " + call.isExecuted());
                Accesstoken = response.body().getAccess_token();
                getContentToken();
            }

            @Override
            public void onFailure(Call<AccessTokenResponseModel> call, Throwable t) {
                Log.e(TAG, "onError: " + t.getMessage());
            }
        });

    }

    private void getContentToken() {
        Call<ContentTokenResponseModel> contentTokenCall = apiService.getContentToken(
                "TEN999-CH002-DASH",
                "device",
                "Bearer " + Accesstoken,
                "nagra");
        contentTokenCall.enqueue(new Callback<ContentTokenResponseModel>() {
            @Override
            public void onResponse(Call<ContentTokenResponseModel> call, Response<ContentTokenResponseModel> response) {
                Log.d(TAG, "onResponse Content_token: " + response.body().getContent_token());
                Log.d(TAG, "onResponse Call: " + call.isExecuted());
                ContentToken = response.body().getContent_token();
//                httpMediaDrmCallback.setContentTokenVal(ContentToken);
                if (player == null)
                    createPlayer();
                httpMediaDrmCallback.setKeyRequestProperty("nv-authorizations", ContentToken);
                //getLicense();
                handler.sendEmptyMessageDelayed(CALL_API, 300000);
            }

            @Override
            public void onFailure(Call<ContentTokenResponseModel> call, Throwable t) {
                Log.e(TAG, "onError: " + t.getMessage());

            }
        });
    }

    private void getLicense() {
        Call<ResponseBody> licenseTokenCall = licenseService.getContentLicense(
                SSP_TENANT_ID,
                "Bearer " +ContentToken
        );

        licenseTokenCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("Kapil","License Response : "+response);
                try {
                    Response = response.body().bytes();
                    if (player == null)
                        createPlayer();
                    httpMediaDrmCallback.setContentTokenVal(ContentToken);
                    httpMediaDrmCallback.setResponse(Response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onError License Response: " + t.getMessage());
            }
        });
    }
}
