package otang.json.to.java.networking;

public class ApiClient {
	public static ApiInterface getApiInterface() {
		return RetrofitInstance.getClient().create(ApiInterface.class);
	}
}