package com.test.tickersdemo;

import com.google.gson.Gson;
import com.test.tickersdemo.network.TickersRepository;
import com.test.tickersdemo.network.TradernetApi;
import com.test.tickersdemo.ui.TickersViewModel;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Singleton
@Component(modules = TickersComponent.Dependencies.class)
interface TickersComponent {
    static TickersComponent createGraph() {
        return DaggerTickersComponent.create();
    }

    TickersViewModel tickersViewModel();

    @Module
    final class Dependencies {
        @Provides
        @Singleton
        static OkHttpClient okHttpClient() {
            return new OkHttpClient.Builder().build();
        }

        @Provides
        @Singleton
        static Gson gson() {
            return new Gson();
        }

        @Provides
        @Singleton
        static TradernetApi tradernetApi(OkHttpClient client, Gson gson) {
            return new Retrofit.Builder()
                    .baseUrl("https://tradernet.com/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(TradernetApi.class);
        }

        @Provides
        static TickersRepository tickersRepository(TradernetApi api) {
            return new TickersRepository(api);
        }

        @Provides
        static TickersViewModel tickersViewModel(TickersRepository repository) {
            return new TickersViewModel(repository);
        }
    }
}
