package com.example.push_test1;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "my_channel_id1"; // MainActivity의 CHANNEL_ID와 동일해야 합니다.
    private static final int NOTIFICATION_ID = 32; // 원하는 ID로 설정

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // data 메시지에서 정보 가져오기
        String title = "알림";
        String message = "새 메시지가 도착했습니다.";
        String hogi = "";

        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().getOrDefault("title", title);
            message = remoteMessage.getData().getOrDefault("body", message);
            hogi = remoteMessage.getData().getOrDefault("hogi", hogi);
        }

        String notificationMessage = hogi + " 호기 " + message + " 발생";

        Log.d(TAG, "Data received: title=" + title + ", body=" + message + ", hogi=" + hogi);

        // 알림 채널 생성
        createNotificationChannel();

        // Notification 생성
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(notificationMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Heads-up 알림
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            } else {
                Log.w(TAG, "POST_NOTIFICATIONS 권한이 없어 알림을 표시할 수 없습니다.");
            }
        } else {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "기본 채널";
            String description = "앱 알림 채널";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Heads-up 가능
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
