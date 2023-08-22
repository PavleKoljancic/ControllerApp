package test.designe.app.controllerapp.retrofit;

import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import test.designe.app.controllerapp.models.PinUser;
import test.designe.app.controllerapp.models.Route;
import test.designe.app.controllerapp.models.RouteHistory;
import test.designe.app.controllerapp.models.ScanInterraction;
import test.designe.app.controllerapp.models.User;


public interface ControllerAPI {





    @POST("/api/pinusers/login")
    Call<String> loginUser(@Body PinUser PinUser);

    @GET("/api/routesHistory/checkRouteHistoryByTerminalId={TerminalId}")
    Call<RouteHistory> getRouteHistory(@Path("TerminalId") Integer terminalId, @Header("Authorization") String BarerToken);

    @GET("/api/routes/getAll")
    public Call<List<Route>> getAllRoutes( @Header("Authorization") String BarerToken);
    @GET("api/users/getUserById={Id}")
    public Call<User> getUserById(@Path("Id") String UserId, @Header("Authorization") String BarerToken);

    @GET("api/user/files/get/profilepicture&userId={UserId}")
    Call<ResponseBody> getUserProfilePicture(@Path("UserId") Integer UserId, @Header("Authorization") String BarerToken);

    @GET("api/terminals/getScanInterractionsForSameRouteByTerminalId={TerminalId}andNotOlderThan={Minutes}")
    Call<List<ScanInterraction>> getScanInteractions(@Path("TerminalId")Integer terminalId, @Path("Minutes")long minutes, @Header("Authorization") String BarerToken);
}
