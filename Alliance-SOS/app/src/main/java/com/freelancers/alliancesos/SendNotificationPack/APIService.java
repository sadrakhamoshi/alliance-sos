package com.freelancers.alliancesos.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAoFvPnaw:APA91bFOCsKRjeoqBlKvJgl4CPjTt0S7ICAqVjmYDAZ6k1e4WZqNSa52b1pqbtLE1t4Q4Qj6g9pto6-zOrWVuINxNsZOc9OoNEnuhoBVmwkgFX2_SMvSF05o-xLTx-HJt-5Sb5FfOTbz"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body NotificationSender body);
}
