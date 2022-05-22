package otang.json.to.java.networking;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitInstance {
	private static Retrofit retrofit = null;

	public static Retrofit getClient() {
		if (retrofit == null) {
			retrofit = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).build();
		}
		return retrofit;
	}
}