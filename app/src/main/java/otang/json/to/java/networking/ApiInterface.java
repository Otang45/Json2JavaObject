package otang.json.to.java.networking;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiInterface {
	@GET
	Call<String> getData(@Url String url);
}