package com.Emergency.Driver;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.Emergency.Driver.Models.Locate;
import com.Emergency.Driver.Models.Patient;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class SimpleDirectionActivity extends AppCompatActivity implements
        OnMapReadyCallback ,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        RoutingListener
        {

    private LocationCallback mLocationCallback;
    boolean doubleBackToExitPressedOnce = false;
    int patientUpdateCnt=0,mylocUpdateCnt=0;
    LatLng patientloc,ambcurrloc;
    Location mLastLocation;
    Marker ambCurrLocationMarker;
    Marker currPatientMarker;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark,R.color.colorPrimary,R.color.colorAccent,R.color.colorAccent,R.color.primary_dark_material_light};
      private List<Polyline> polylines;

    private Button btnRequestDirection;
    private GoogleMap googleMap;
    private String serverKey = "AIzaSyDi3B9R9hVpC9YTmOCCz_pCR1BKW3tIRGY";
  //      private LatLng origin = new LatLng(21.218681, 80.307411);
  //  private LatLng destination = new LatLng(21.212248, 81.316434);
    TextView distance, duration,mylonglat,amblonglat;
    private LatLng origin = null;
    private LatLng destination = null;

            private  String  TargetPatient=null;

     private List<Patient> patientList=new ArrayList<>();
    DatabaseReference ambRef,ambLocRef,PatientsRef,targetPatientRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_direction);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        
        PatientsRef=database.getReference("patients");
        ambRef = database.getReference("ambulance");
        ambLocRef = ambRef.child("location");
        targetPatientRef=ambRef.child("TargetPatient");


        distance = findViewById(R.id.distance);
        duration = findViewById(R.id.duration);
        mylonglat=findViewById(R.id.mylonglat);
        amblonglat=findViewById(R.id.amblonglat);


        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);



        final LocationManager manager = (LocationManager)getApplicationContext().getSystemService    (Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ){
            AlertDialog alertDialog = new AlertDialog.Builder(
                    SimpleDirectionActivity.this).create();

            // Setting Dialog Title
            alertDialog.setTitle("Alert! Turn On GPS");

            // Setting Dialog Message
            alertDialog.setMessage("Please turn on Location Service to use this app...");

            // Setting Icon to Dialog
//            alertDialog.setIcon(R.drawable.tick);

            // Setting OK Button
            alertDialog.setButton("Turn On GPS", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Write your code here to execute after dialog closed
                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getApplicationContext().startActivity(intent);
                }
            });

            // Showing Alert Message
            alertDialog.show();
            alertDialog.setCancelable(false);
        }
        else{

        }



        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (final Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    mylocUpdateCnt+=1;
                    mylonglat.setText(mylocUpdateCnt + " my location :"+location.getLatitude()+","+location.getLongitude());

                    ambLocRef.setValue(new Locate(location.getLatitude(),location.getLongitude()));
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    ambcurrloc=latLng;
                    mLastLocation = location;
                    if (ambCurrLocationMarker != null) {
                   //     Toast.makeText(getApplicationContext(),"my location changed",Toast.LENGTH_SHORT).show();
                        ambCurrLocationMarker.setPosition(latLng);

                    }else{
                        Toast.makeText(getApplicationContext(),"my location set",Toast.LENGTH_SHORT).show();
                        origin =new LatLng(location.getLatitude(),location.getLongitude());
                        Toast.makeText(SimpleDirectionActivity.this,location.getLatitude()+":"+location.getLongitude(),Toast.LENGTH_LONG).show();

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("Amb Current Position");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        ambCurrLocationMarker = googleMap.addMarker(markerOptions);

                        //..................................................................................................................................
                        Button button=findViewById(R.id.zoomin);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                            }
                        });
                    }
//....................................................................................................................................


                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                }googleMap.animateCamera(CameraUpdateFactory.zoomTo(16));

            }
        };


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
        }


    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }




    @Override
    protected void onStart() {
        super.onStart();
        //  requestDirection();
        checkLocationPermission();

            targetPatientRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("target patient listener :" + dataSnapshot.toString());
                    if(polylines!=null) {
                        polylines.clear();
                    }
                    if (dataSnapshot.getValue() != null) {
                        TargetPatient = dataSnapshot.getValue().toString();

                        if(ambcurrloc!=null && patientloc!=null){
                            //  new Fetch_direction_time().execute(AmbLocation.latitude,AmbLocation.longitude,patientInitialLoc.latitude,patientInitialLoc.longitude);
                            Routing routing = new Routing.Builder()
                                    .travelMode(Routing.TravelMode.DRIVING)
                                    .withListener(SimpleDirectionActivity.this)
                                    .key(serverKey)
                                    .waypoints(ambcurrloc,patientloc)
                                    .build();
                            routing.execute();

                        }


                    } else {
                        distance.setText("");
                        duration.setText("");
                        TargetPatient = null;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            PatientsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) {
                        Button button = findViewById(R.id.btn_TaskCompInform);
                        button.setClickable(false);
                        button.setText("No Task");
                        distance.setText("");
                        duration.setText("");
                        TargetPatient = null;

                        targetPatientRef.removeValue();
                    } else if (dataSnapshot.getValue() != null) {
                        System.out.println("patientsref ,listener" + dataSnapshot.toString());
                        System.out.println("patient count :" + dataSnapshot.getChildrenCount());
                        Button button = findViewById(R.id.btn_TaskCompInform);
                        button.setClickable(true);
                        button.setText("Current Task Completed");

                        patientList = new ArrayList<Patient>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Patient p = snapshot.getValue(Patient.class);
                            patientList.add(snapshot.getValue(Patient.class));
                            System.out.println(snapshot.getValue(Patient.class));
                            System.out.println("patient :" + new Gson().toJson(p));
                        }

                        System.out.println("patient map :" + Arrays.deepToString(patientList.toArray()));

                        PatientsRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                System.out.println("on child added :");
                                System.out.println("DataSnapshot :" + dataSnapshot);

                                if (patientList != null && patientList.size() == 1) {
                                    TargetPatient = patientList.get(0).getId();
                                    targetPatientRef.setValue(patientList.get(0).getId());
                                    Patient patient = dataSnapshot.getValue(Patient.class);
                                    System.out.println("snapshot value :" + new Gson().toJson(patient));

                                    if (patient != null) {
                                        Locate location = new Locate(dataSnapshot.getValue(Patient.class).getLatitude(), dataSnapshot.getValue(Patient.class).getLongitude());
                                        setPatientMarker(location);
                                    }
                                } else {

                                }

                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                System.out.println("on child changed :");
                                System.out.println("DataSnapshot :" + dataSnapshot);

                                if (TargetPatient != null) {
                                    if (dataSnapshot.getKey().equals(TargetPatient)) {
                                        try {
                                            System.out.println("patient location changed....");

                                            Locate location = new Locate(dataSnapshot.getValue(Patient.class).getLatitude(), dataSnapshot.getValue(Patient.class).getLongitude());
                                            patientloc=new LatLng(dataSnapshot.getValue(Patient.class).getLatitude(), dataSnapshot.getValue(Patient.class).getLongitude());
                                            setPatientMarker(location);
                                            Log.d("on changed listener ,", "Patient location: " + new Gson().toJson(location));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                                System.out.println("on child removed :");
                                System.out.println("DataSnapshot :" + dataSnapshot);
                                System.out.println("last alotted patient :" + TargetPatient);

                                if (TargetPatient != null) {
                                    if (patientList != null && patientList.size() > 0 && dataSnapshot.getKey().equals(TargetPatient)) {
                                        TargetPatient = patientList.get(0).getId();
                                        System.out.println("new alotted patient :" + TargetPatient);
                                        targetPatientRef.setValue(TargetPatient);
                                    }
                                }
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                System.out.println("on child moved :");
                                System.out.println("DataSnapshot :" + dataSnapshot);
                                System.out.println(" s :" + s);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                System.out.println(databaseError);
                            }
                        });


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("patientsref ,listener :" + databaseError);
                }
            });


    }




            void setPatientMarker(Locate location){
        try {
            if (location != null && location.getLatitude() != null && location.getLatitude() != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                patientUpdateCnt += 1;
                amblonglat.setText(patientUpdateCnt + " patient:" + location.getLatitude() + "," + location.getLongitude());

                if (currPatientMarker != null) {
                    Toast.makeText(getApplicationContext(), "patient location changed...", Toast.LENGTH_SHORT).show();
                    System.out.println("patient marker already created....");
                    currPatientMarker.setPosition(latLng);
                } else {
                    Toast.makeText(getApplicationContext(), "patient location set", Toast.LENGTH_SHORT).show();
                    System.out.println("no marker ......creating");
                    destination = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("patient Position");
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    currPatientMarker = googleMap.addMarker(markerOptions);
                }
            }
        }catch (Exception e){
            e.printStackTrace();

        }
    }






    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
     //   mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
         LocationServices.getFusedLocationProviderClient(getApplicationContext()).requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        googleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


        public void TaskCompInform(View view) {
            System.out.println("current task completed button clked");
            System.out.println("target patient :" + TargetPatient);
            if (TargetPatient == null) {
                Toast.makeText(getApplicationContext(),"target patient null",Toast.LENGTH_SHORT).show();
             } else if(TargetPatient!=null){
                System.out.println("patientref :" + PatientsRef.child(TargetPatient).toString());
                PatientsRef.child(TargetPatient).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            System.out.println("task success,current patient removed from list");
                            Toast.makeText(getApplicationContext(), "Last patient removed", Toast.LENGTH_SHORT).show();
                        } else {
                            System.out.println("task unsuccess,current patient not removed from list");
                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
            //........................................................................................................................
            @Override
            public void onBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "ALERT! click BACK again to \"Cancel Request\" and \"Exit\"", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2000);
            }
            //..........................................................................................................................
            @Override
            public void onRoutingFailure(RouteException e) {
                e.printStackTrace();
            }

            @Override
            public void onRoutingStart() {

            }

            @Override
            public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
                System.out.println("found the route :routing success");
                polylines = new ArrayList<>();
                //add route(s) to the map.
                 distance.setVisibility(View.VISIBLE);
                 duration.setVisibility(View.VISIBLE);
                distance.setText(route.get(0).getDistanceText());
                duration.setText(route.get(0).getDurationText());
                for (int i = 0; i <route.size(); i++) {

                    //In case of more than 5 alternative routes
                    int colorIndex = i % COLORS.length;

                    PolylineOptions polyOptions = new PolylineOptions();
                    polyOptions.color(getResources().getColor(COLORS[colorIndex]));
                    polyOptions.width(10 + i * 3);
                    polyOptions.addAll(route.get(i).getPoints());
                    Polyline polyline = googleMap.addPolyline(polyOptions);
                    polylines.add(polyline);

            //        Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+
                }
            }

            @Override
            public void onRoutingCancelled() {

            }
        }
