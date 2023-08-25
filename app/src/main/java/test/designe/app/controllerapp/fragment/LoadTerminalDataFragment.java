package test.designe.app.controllerapp.fragment;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.io.IOException;

import retrofit2.Response;
import test.designe.app.controllerapp.LoginActivity;
import test.designe.app.controllerapp.R;
import test.designe.app.controllerapp.TokenManager;
import test.designe.app.controllerapp.models.RouteHistory;
import test.designe.app.controllerapp.nfc.ControllerAppHostApduService;
import test.designe.app.controllerapp.nfc.IdReadSubscriber;
import test.designe.app.controllerapp.retrofit.ControllerAPI;
import test.designe.app.controllerapp.retrofit.RetrofitService;


public class LoadTerminalDataFragment extends Fragment implements IdReadSubscriber {

    HandlerThread handlerThread;

    Button LogOutButton;
    CircularProgressIndicator progressIndicator;
    TextView infoText;

    ControllerAPI api;

    public LoadTerminalDataFragment() {

    }

    @Override
    public void onStart() {
        api = RetrofitService.getApi();
        this.handlerThread = new HandlerThread("API Call Thread Load Terminal");
        this.handlerThread.start();

        infoText = (TextView) getView().findViewById(R.id.inforTextLoadTerminal);

        LogOutButton = (Button) getView().findViewById(R.id.LogOutBtn);
        progressIndicator = (CircularProgressIndicator) getView().findViewById(R.id.progressTerminal);



        LogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


                builder.setMessage("Da li žeilte da se odjavite?")
                        .setTitle("Kraj rada");
                builder.setPositiveButton("Ne", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                builder.setNegativeButton("Da", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        getActivity().finish();
                    }
                });
                builder.setCancelable(false);

                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });

        Handler handler = new Handler(handlerThread.getLooper());



        super.onStart();
    }

    @Override
    public void onResume() {
        getActivity().startService(new Intent(getActivity(), ControllerAppHostApduService.class));
        Handler handler = new Handler(handlerThread.getLooper());

        handler.post(() ->{
        boolean result =ControllerAppHostApduService.subscribeToIdRead(this);});
        super.onResume();
    }

    @Override
    public void onPause() {
        getActivity().stopService(new Intent(getActivity(), ControllerAppHostApduService.class));
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_load_terminal_data, container, false);

    }

    private void loadTerminalFromId(Integer terminalId) {


        if (terminalId != null) {

            Handler handler = new Handler(handlerThread.getLooper());
            progressIndicator.setVisibility(View.VISIBLE);
            final Integer finalTerminalId = terminalId;
            handler.post(() -> {

                callGetRouteHistory(finalTerminalId, this);
            });
        }

    }

    private void callGetRouteHistory(Integer terminalId, final LoadTerminalDataFragment parent) {
        {
            try {
                Response<RouteHistory> response = api.getRouteHistory(terminalId, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute();
                if (response.isSuccessful() && response.body() != null) {

                    RouteFragment routeFragment = new RouteFragment();
                    routeFragment.setParentFragment(parent);
                    routeFragment.setRouteHistory(response.body());
                    getActivity().runOnUiThread(() ->

                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, routeFragment).commit()

                    );

                } else if (!response.isSuccessful())
                    getActivity().runOnUiThread(() -> infoText.setText("Desila se greška")


                    );


            } catch (IOException e) {
                getActivity().runOnUiThread(() ->


                        infoText.setText("Desila se greška")
                );
            } finally {
                getActivity().runOnUiThread(() -> {
                    progressIndicator.setVisibility(View.INVISIBLE);

                });
            }

        }
    }

    @Override
    public void onDestroy() {
        this.handlerThread.quit();
        ControllerAppHostApduService.unsubscribeToIdRead(this);
        getActivity().stopService(new Intent(getActivity(), ControllerAppHostApduService.class));
        super.onDestroy();
    }

    @Override
    public void onIdRead(Integer id) {
        getActivity().runOnUiThread(() -> loadTerminalFromId(id));
    }
}