package demo.com.gdmec07140510.work7;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import java.text.DecimalFormat;
import java.util.IllegalFormatCodePointException;
import java.util.logging.LogRecord;

public class GPSActivity extends AppCompatActivity implements Handler.Callback {
    MapView mMapView = null;
    BaiduMap mBaiduMap = null;
    private Marker mMarkerGPS;
    private Marker mMarkerGSM;
    private LatLng position = null;
    BitmapDescriptor bd = null;
    private Double gsmLng = 0.0;
    private Double gsmLat = 0.0;
    private Double gpsLng = 0.0;
    private Double gpsLat = 0.0;
    private Handler locationHandler;
    private Bitmap bmp;
    public final static int Gps_Location = 1;
    public final static int Gsm_Location = 2;
    private final int MENU_GSM = 1;
    private final int MENU_GPS = 2;
    private final int MENU_DIS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_gps);
        bd = BitmapDescriptorFactory.fromResource(R.drawable.speed_port);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        position = new LatLng(23.148059, 113.329632);
        MapStatus mMapStatus = new MapStatus.Builder().target(position).zoom(16).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        MarkerOptions ooGPS = new MarkerOptions().position(position).icon(bd).zIndex(9).draggable(true);
        mMarkerGPS = (Marker) mBaiduMap.addOverlay(ooGPS);
        MarkerOptions ooGSM = new MarkerOptions().position(position).icon(bd).zIndex(9).draggable(true);
        mMarkerGSM = (Marker) mBaiduMap.addOverlay(ooGSM);
        locationHandler = new Handler(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        bd.recycle();
    }

    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Gps_Location:
                gpsLng = msg.getData().getDouble("longitude");
                gpsLat = msg.getData().getDouble("latitude");
                position = new LatLng(gpsLat, gpsLng);
                Log.d("00385", "" + position.latitude + ":" + position.longitude);
                MapStatus mMapStatusGPS = new MapStatus.Builder().target(position).zoom(18).build();
                MapStatusUpdate mMapStatusUpdateGPS = MapStatusUpdateFactory.newMapStatus(mMapStatusGPS);
                mBaiduMap.animateMapStatus(mMapStatusUpdateGPS);
                mMarkerGPS = null;
                MarkerOptions ooGPS = new MarkerOptions().position(position).icon(bd).zIndex(9).draggable(true);
                mMarkerGPS = (Marker) mBaiduMap.addOverlay(ooGPS);
                Toast.makeText(this, "GPS定位成功！", Toast.LENGTH_SHORT).show();
                break;
            case Gsm_Location:
                gsmLng = msg.getData().getDouble("longitude");
                gsmLat = msg.getData().getDouble("latitude");
                position = new LatLng(gpsLat, gpsLng);
                MapStatus mMapStatusGSM = new MapStatus.Builder().target(position).zoom(18).build();
                MapStatusUpdate mMapStatusUpdateGSM = MapStatusUpdateFactory.newMapStatus(mMapStatusGSM);
                mBaiduMap.animateMapStatus(mMapStatusUpdateGSM);
                mMarkerGSM = null;
                MarkerOptions ooGSM = new MarkerOptions().position(position).icon(bd).zIndex(9).draggable(true);
                mMarkerGSM = (Marker) mBaiduMap.addOverlay(ooGSM);
                Toast.makeText(this, "基站定位成功！", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_GPS, 0, "GPS定位");
        menu.add(0, MENU_GSM, 0, "基站定位");
        menu.add(0, MENU_DIS, 0, "误差计算");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_GPS) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "请开启GPS!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
                return false;
            }
            gpsLocation(locationManager);
            return true;
        } else if (item.getItemId() == MENU_GSM) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            gsmLocation(locationManager);
        } else if (item.getItemId() == MENU_DIS) {
            if (gpsLng != 0.0 && gsmLng != 0.0) {
                double result = getDistance(gpsLat, gpsLng, gsmLat, gsmLng);
                DecimalFormat df = new DecimalFormat(".##");
                Toast.makeText(this, "误差值为：" + df.format(result) + "*", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == 4) {
            position = new LatLng(23.148059, 113.329632);
            MapStatus mMapStatus = new MapStatus.Builder().target(position).zoom(16).build();
            MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            mBaiduMap.setMapStatus(mMapStatusUpdate);
        }
        return super.onOptionsItemSelected(item);
    }

    private double getDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    private void gsmLocation(LocationManager tm) {
        Location location = tm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null){
            location = tm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location!=null){
            Bundle bundle = new Bundle();
            bundle.putDouble("longitude",location.getLongitude());
            bundle.putDouble("latitude",location.getLatitude());
            Message gsm_Msg = Message.obtain(locationHandler,Gsm_Location);
            gsm_Msg.setData(bundle);
            gsm_Msg.sendToTarget();
        }else {
            Toast.makeText(GPSActivity.this,"基站获取失败，"+"请确保AGPS，GPS已被打开",Toast.LENGTH_SHORT).show();
        }
    }

    private void gpsLocation(LocationManager locMan) {
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER,30000,10,Gps_locationListener);
    }
    private LocationListener Gps_locationListener = new LocationListener(){
        @Override
        public void onLocationChanged(Location location) {
            Bundle bundle = new Bundle();
            bundle.putDouble("longitude",location.getLongitude());
            bundle.putDouble("latitude",location.getLatitude());
            Message gps_Msg = Message.obtain(locationHandler,Gps_Location);
            gps_Msg.setData(bundle);
            gps_Msg.sendToTarget();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    protected boolean isRouteDisplayed(){
        return true;
    }
}
