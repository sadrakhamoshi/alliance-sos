package com.kaya.alliancesos.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAoE_ORls:APA91bHs5YgknL4gvOPvDphjQYnl713zp2ImDzVCNXbDFVTaorNbOYPadPK2Hb7acW3eHLLPBxYiafBfKiLSJI0LteI6M2C-HH52hYwWb8tIuB8U-cZv7r1Zb8eGR3cdfz__kvhUDZqP"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body NotificationSender body);

}
