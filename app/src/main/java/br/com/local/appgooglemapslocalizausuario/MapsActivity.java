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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
        //obtem suporte do fragment para carregar o mapa.
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
                // tente comentar essa linha e você verá que seu ponto de localização anteriror não apagará
                mMap.clear();

                //Recupera informações do local do usuário
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    //Recupera o endereço do usuário
                    List<Address> listaEndereco = geocoder.getFromLocation(latitude, longitude, 1);

                    //Recuperar o local pelo endereço
                    String enderecoLocal = "Av. Feliciano Correia, s/n - Jardim Satelite, São Paulo - SP, 04815-240";
                    //List<Address> listaEndereco = geocoder.getFromLocationName(enderecoLocal, 1);

                    //testando se realmente temos um endereço
                    if (listaEndereco != null && listaEndereco.size() > 0) {
                        //se quiser utilizar uma estrutura de repetição pode pegar a lista de endereço toda
                        Address endereco = listaEndereco.get(0);
                        //Log.d("local", "onLocationChanged: " + endereco.getAddressLine(0));

                        //Posicionando o marcador com base no endereço do usuário

                        Double lat = endereco.getLatitude();
                        Double lon = endereco.getLongitude();
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                        //Criando marcador com o endereço do usuário
                        LatLng localUsuario = new LatLng(lat, lon);

                        mMap.addMarker(new MarkerOptions()
                                .position(localUsuario)
                                .title("Local atual")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.loc))
                        );
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localUsuario, 15));

                        /*
                         * D/local: onLocationChanged:
                         * Address[addressLines=[0:"R. Bonsucesso, 60 - Jardim Noronha, São Paulo - SP, 04853-192, Brazil"],
                         * feature=60,
                         * admin=São Paulo,
                         * sub-admin=São Paulo,
                         * locality=null,
                         * thoroughfare=Rua Bonsucesso,
                         * postalCode=04853-192,
                         * countryCode=BR,
                         * countryName=Brazil,
                         * hasLatitude=true,
                         * latitude=-23.7716203,
                         * hasLongitude=true,
                         * longitude=-46.6768499,
                         * phone=null,
                         * url=null,
                         * extras=null]

                         * */


                        Log.d("local", "onLocationChanged: " + endereco.toString());
                        //txtNomeEndereco.setText(endereco.getAddressLine(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, 10,
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