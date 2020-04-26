package in.geekofia.igdl.interfaces;

import in.geekofia.igdl.models.ShortUrl;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ShortenApi {

        @GET("create.php")
        Call<ShortUrl> getShortURL(@Query("format") String format, @Query("url") String url);

//        @GET("create.php")
//        Call<ShortUrl> getShortURLStats(@Query("format") String format, @Query("url") String url, @Query("logstats") int statsEnabled);
}
