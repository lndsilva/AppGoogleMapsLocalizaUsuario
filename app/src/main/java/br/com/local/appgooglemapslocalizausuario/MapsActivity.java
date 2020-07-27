package br.com.local.appgooglemapslocalizausuario;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //Criando array de strings para as permissões do maps
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    //Gerenciador de localização
    private LocationManager locationManager;
    //Lista de localizações
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Validando as permissões
        Permissoes.validarPermissoes(permissoes, this, 1);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Criando objeto para gerenciar localização do usuário
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Instânciando o listener de localizações para ser utilizado no onRequestPermission
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d("Localização", "onLocationChanged: " + location.toString());

                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();

                //limpando os marcadores para não repetir no mapa
                mMap.clear();

                /*

                */
                //Recupera informações do local do usuário
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    //Recupera o endereço do usuário
                    // List<Address> listaEndereco = geocoder.getFromLocation(latitude, longitude, 1);

                    //Recuperar o local pelo endereço
                    String enderecoLocal = "AV ROBERTO KENNEDY, 4695 - Interlagos, São Paulo - SP, 04772-005";
                    List<Address> listaEndereco = geocoder.getFromLocationName(enderecoLocal, 1);

                    //testando se realmente temos um endereço
                    if (listaEndereco != null && listaEndereco.size() > 0) {
                        //se quiser utilizar uma estrutura de repetição pode pegar a lista de endereço toda
                        Address endereco = listaEndereco.get(0);
                        //Log.d("local", "onLocationChanged: " + endereco.getAddressLine(0));

                        //Posicionando o marcador com base no endereço do usuário

                        Double lat = endereco.getLatitude();
                        Double lon = endereco.getLongitude();

                        LatLng localUsuario = new LatLng(lat, lon);
                        mMap.addMarker(new MarkerOptions().position(localUsuario).title("Local"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localUsuario, 16));


                        Log.d("local", "onLocationChanged: " + endereco.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, 0,
                    locationListener
            );
        }


    }

    //Criando a janela para permissões do usuário a sua localização
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Percorrendo a permissão do usuário
        for (int permissaoResultado : grantResults) {
            //Se permissão for negada
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                //Mostra um alerta
                validacaoUsuario();

            }
            //Se permissão for concedida
            else if (permissaoResultado == PackageManager.PERMISSION_GRANTED) {
                //Recupera localização do usuário
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0, 0,
                            locationListener
                    );
                }

            }
        }
    }

    //Criando o alertDialog
    private void validacaoUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissão negada!!!");
        builder.setMessage("Para utilizar o App é necessário aceitar as permissões!!!");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}