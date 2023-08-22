package test.designe.app.controllerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.widget.Toast;

import java.nio.ByteBuffer;

import test.designe.app.controllerapp.fragment.LoadTerminalDataFragment;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoadTerminalDataFragment loadTerminalDataFragment = new LoadTerminalDataFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,loadTerminalDataFragment).commit();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }




}