/*
 *  This file is part of the IOTA Access distribution
 *  (https://github.com/iotaledger/access)
 *
 *  Copyright (c) 2020 IOTA Stiftung.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.iota.access.di.module;

import android.app.Application;
import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.iota.access.BuildConfig;
import org.iota.access.api.APILibDacAuthNative;
import org.iota.access.api.Communicator;
import org.iota.access.api.CommunicatorImpl;
import org.iota.access.api.CommunicatorStub;
import org.iota.access.api.OnMessageReceived;
import org.iota.access.api.PSService;
import org.iota.access.api.TSService;
import org.iota.access.api.asr.ASRClient;
import org.iota.access.api.tcp.TCPClient;
import org.iota.access.api.tcp.TCPClientImpl;
import org.iota.access.api.udp.UDPClient;
import org.iota.access.data.DataProvider;
import org.iota.access.data.DataProviderImpl;
import org.iota.access.di.AppSharedPreferences;
import org.iota.access.utils.ResourceProvider;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Module for providing components used by the application
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
@Module(includes = ViewModelModule.class)
public class AppModule {

    private final String SSL_PROTOCOL = "SSL";

    private final String TOKEN_SERVER_URL = BuildConfig.TOKEN_SERVER_URL;

    private final String POLICY_SERVER_URL = BuildConfig.POLICY_SERVER_URL;

    @Singleton
    @Provides
    public ASRClient provideASRClient(Context context) {
        return new ASRClient(context);
    }

    @Provides
    @Singleton

    public Communicator providesCommunicator(TCPClient tcpClient, TSService tsService, PSService psService) {
        if (BuildConfig.STUB_DEVICE_COMUNICATOR) {
            return new CommunicatorStub(tsService, psService);
        } else {
            return new CommunicatorImpl(tcpClient);
        }
    }

    @Singleton
    @Provides
    public TCPClient provideTCPClient(AppSharedPreferences preferences, APILibDacAuthNative apiLibDacAuthNative) {
        return new TCPClientImpl(preferences, apiLibDacAuthNative);
    }

    @Provides
    public ResourceProvider provideResourceProvider(Context context) {
        return new ResourceProvider(context);
    }

    /**
     * Provides {@link UDPClient}
     *
     * @param listener listener that receives messages
     * @return a {@link UDPClient} object
     */
    @Provides
    public UDPClient provideUDPClient(OnMessageReceived listener) {
        return new UDPClient(listener);
    }

    /**
     * Provides HTTP Cache
     *
     * @param application Application context
     * @return a {@link Cache} object
     */
    @Provides
    // should be created only once
    @Singleton
    public Cache provideHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024;
        return new Cache(application.getCacheDir(), cacheSize);
    }

    /**
     * Provide Gson
     *
     * @return {@link Gson} object
     */
    @Provides
    // should be created only once
    @Singleton
    public Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }

    /**
     * Provides the HTTP client
     *
     * @param cache The cache that should be used
     * @return {@link OkHttpClient} object
     */
    @Provides
    // should be created only once
    @Singleton
    public OkHttpClient provideOkhttpClient(Cache cache, Context context) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.cache(cache);
        client.connectTimeout(40, TimeUnit.SECONDS);
        client.readTimeout(40, TimeUnit.SECONDS);
        // interceptor for logging
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.addInterceptor(logInterceptor);

        client.hostnameVerifier((hostname, session) -> true);

        // bypass servers without trusted certificates, most probably those which are not secured (http)
        SSLSocketFactory factory = getSSLSocketFactory();
        if (factory != null) {
            client.sslSocketFactory(factory, getTrustManager());
        }
        client.hostnameVerifier((hostname, session) ->
                true
        );

        return client.build();
    }

    @Provides
    @Singleton
    public TSService provideTSService(Gson gson, OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(TOKEN_SERVER_URL)
                .client(okHttpClient)
                .build();
        return retrofit.create(TSService.class);
    }

    @Provides
    @Singleton
    public PSService providePService(Gson gson, OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(POLICY_SERVER_URL)
                .client(okHttpClient)
                .build();
        return retrofit.create(PSService.class);
    }

    /**
     * Helper method for bypassing servers without trusted certificates
     *
     * @return {@link SSLSocketFactory}
     */
    private SSLSocketFactory getSSLSocketFactory() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            return sslContext.getSocketFactory();

        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Helper method for providing {@link X509TrustManager}
     *
     * @return {@link X509TrustManager}
     */
    private X509TrustManager getTrustManager() {
        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (trustManagerFactory != null) {
            try {
                trustManagerFactory.init((KeyStore) null);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }

            return (X509TrustManager) trustManagers[0];
        }
        return null;
    }

    @Provides
    @Singleton
    public DataProvider providesDataProvider() {
        return new DataProviderImpl();
    }
}
