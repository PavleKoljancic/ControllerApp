package test.designe.app.controllerapp.retrofit;

import com.google.gson.Gson;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitService {





     private static Retrofit retrofit;

    private static ControllerAPI api;

    public static ControllerAPI getApi()
    {
        if(api==null)
        {
            if(retrofit==null)
                initializeRetrofit();
           api = retrofit.create(ControllerAPI.class);
        }

        return api;
    }

    private static void initializeRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(MyURL.getURL())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();
    }

}

