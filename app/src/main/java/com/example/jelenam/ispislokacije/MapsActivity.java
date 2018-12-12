package com.example.jelenam.ispislokacije;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String nazivLokacije;
    static int brojac = 0;
    private Marker marker;
    ArrayList<Marker> markers;
    Context context = this;
    ArrayList<Polyline> ruta;
    ArrayList<LatLng> koordinate;
    private LatLng beograd= new LatLng(44.77866,20.4489);
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        markers = new ArrayList<>();
        ruta = new ArrayList<>();
        koordinate = new ArrayList<>();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(beograd, 7));

        //DODAVANJE MARKERA NA DODIR
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                brojac++;

                koordinate.add(latLng);
                nazivLokacije = saznajLokaciju(MapsActivity.this,latLng.latitude,latLng.longitude);

                // Postavljanje markera na lokaciju na koju smo kliknuli
                marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("lokacija")
                        .snippet(nazivLokacije)
                        .draggable(true)
                );
                markers.add(marker);
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.markerslikao, ""+markers.size())));


                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

               if(koordinate.size()>1){
                   for(int z = 0; z < koordinate.size() - 1; z++) {

                       LatLng src = koordinate.get(z);
                       LatLng dest = koordinate.get(z + 1);



                           ruta.add(mMap.addPolyline(new PolylineOptions()
                                   .add(new LatLng(src.latitude, src.longitude),
                                           new LatLng(dest.latitude, dest.longitude))
                                   .color(Color.RED)
                                   .geodesic(true)));
                       }
                   }
               }

        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
              brojac--;
                markers.remove(marker);
                marker.remove();
                koordinate.clear();

                for (Polyline line: ruta){
                    line.remove();
                }
                ruta.clear();

                for (int i = 0; i < markers.size(); i++) {
                    Marker m = markers.get(i);
                    LatLng latlng = m.getPosition();
                    nazivLokacije = saznajLokaciju(MapsActivity.this,latlng.latitude,latlng.longitude);
                    koordinate.add(latlng);
                    int redni_br = i+1;
                    m.setIcon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.markerslikao, ""+redni_br)));

                    if (koordinate.size()>1) {
                        for (int z = 0; z < koordinate.size() - 1; z++) {
                            LatLng src = koordinate.get(z);
                            LatLng dest = koordinate.get(z + 1);

                            ruta.add(mMap.addPolyline(new PolylineOptions()
                                    .add(new LatLng(src.latitude, src.longitude),
                                            new LatLng(dest.latitude, dest.longitude))
                                    .color(Color.RED)
                                    .geodesic(true)));
                        }
                    }

                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {

                View v = getLayoutInflater().inflate(R.layout.info_win, null);
                LatLng latLng = marker.getPosition();

               TextView nazivL = (TextView) v.findViewById(R.id.textView);
               String tekst = marker.getSnippet().toString();
               nazivL.setText(tekst);

                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info_win, null);
                LatLng latLng = marker.getPosition();

                TextView nazivL = (TextView) v.findViewById(R.id.textView);
                String tekst = marker.getSnippet().toString();
                nazivL.setText(tekst);
                return v;

            }
        });
        
    }

    private Bitmap writeTextOnDrawable(int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);//za font

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
       // paint.setTextSize(50);
        paint.setTextSize(convertToPixels(context, 15));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(context, 7));        //Scaling needs to be used for different dpi's

        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return  bm;
    }


    public static int convertToPixels(Context context, int nDP) {
        final float conversionScale = context.getResources().getDisplayMetrics().density;
        return (int) ((nDP * conversionScale) + 0.5f) ;
    }

    private String saznajLokaciju(MapsActivity mapsActivity, double lat, double lon) {

        String lokacija = "";
        Geocoder geocoder = new Geocoder(mapsActivity, Locale.getDefault());
        try {
            List<Address> listaLokacija = geocoder.getFromLocation(lat,lon,1);

            Address adresa = listaLokacija.get(0);
            lokacija = adresa.getAddressLine(0);
           // Toast.makeText(getApplicationContext(),""+ listaLokacija.isEmpty(),Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        return lokacija;
    }
}
