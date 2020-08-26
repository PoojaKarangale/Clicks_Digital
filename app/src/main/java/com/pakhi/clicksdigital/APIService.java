package com.pakhi.clicksdigital;

import com.pakhi.clicksdigital.Notifications.MyResponse;
import com.pakhi.clicksdigital.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAoWmSowQ:APA91bG1toHVOP11H5Rxz-eqT8OH-9qBFNd4oCajvaacxWtlbVj7b1kvbp9v9_LfbmJlbtJyN-NspjbM0d8WU8qXNHtk7H0fAW9ogqb29BQrqcKhoPESgU_weV5IlciiEldyN9KhY5JH"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
