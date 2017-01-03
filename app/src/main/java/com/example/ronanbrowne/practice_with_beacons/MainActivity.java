package com.example.ronanbrowne.practice_with_beacons;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.NotificationManager;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // monitering for bike there // stolen - every 30 seconds, unagustable

//Let’s say we defined a “terminal Z” region.
// With monitoring, our app will be notified whenever the user enters and exits the terminal.
// But if we start ranging for the exact same region,
// we’ll instead get a full list of matching beacons currently in range—complete with their UUID, major, and minor values.


    TextView text;
    BeaconManager beaconManager;
    BeaconManager beaconManagerTwo;
    ArrayList<String> beaconsUUIDInrange = new ArrayList<>();

    //used to start stop ranging
    private Region region;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.text);

        beaconManager = new BeaconManager(getApplicationContext());


        //moinitering
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        43739, 63406));
                Log.v("**test", "connect");
            }
        });

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                showNotification("Beacon in range","got ya");
                text.setText("monitoring: In Range "+list.get(0).getMinor());
                Log.v("**test", "enter");

            }

            @Override
            public void onExitedRegion(Region region) {
                showNotification("Beacon out of range","good luck");
                text.setText("monitoring: out of Range");
                Log.v("**test", "exit");

            }
        });


     //   ranging
        beaconManagerTwo = new BeaconManager(this);
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);


        //ranginlistener
        beaconManagerTwo.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);

                    nearestBeacon.getMinor();
                    List<String> places = placesNearBeacon(nearestBeacon);
                    // TODO: update the UI here
                    Log.v("**test", "Nearest places: " + places);
                    beaconsUUIDInrange.clear();;
                    for (Beacon temp: list){

                        Log.v("**test", "Nearest places: " + temp.getMajor());

                        // list of all nearby beacons.
                        beaconsUUIDInrange.add(String.format("%d:%d", temp.getMajor(), temp.getMinor()));
                }

                    Log.v("**test**Output", Arrays.toString(beaconsUUIDInrange.toArray()));
                }
            }
        });



    }



    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManagerTwo.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManagerTwo.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
  beaconManagerTwo.stopRanging(region);
        super.onPause();
    }


    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }



    // could map beacon ID to users bike data:
//
    private static final Map<String, List<String>> PLACES_BY_BEACONS;
    // TODO: replace "<major>:<minor>" strings to match your own beacons.

//a static initializer block,
// which is used to initialize static members of the class.
// It is executed when the class is initialized.

    static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("43739:63406", new ArrayList<String>() {{
            add("Heavenly Sandwiches");
            // read as: "Heavenly Sandwiches" is closest
            // to the beacon with major 22504 and minor 48827
            add("Green & Green Salads");
            // "Green & Green Salads" is the next closest
            add("Mini Panini");
            // "Mini Panini" is the furthest away
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }

}


