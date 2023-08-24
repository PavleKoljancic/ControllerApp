package test.designe.app.controllerapp.fragment;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import test.designe.app.controllerapp.R;
import test.designe.app.controllerapp.nfc.IdReadSubscriber;
import test.designe.app.controllerapp.nfc.ReaderNFC;
import test.designe.app.controllerapp.TokenManager;
import test.designe.app.controllerapp.models.Route;
import test.designe.app.controllerapp.models.RouteHistory;
import test.designe.app.controllerapp.models.ScanInterraction;
import test.designe.app.controllerapp.models.User;
import test.designe.app.controllerapp.retrofit.ControllerAPI;
import test.designe.app.controllerapp.retrofit.RetrofitService;


public class RouteFragment extends Fragment implements IdReadSubscriber {

    List<ScanInterraction> scanInterractions;
    HandlerThread handlerThread;
    LoadTerminalDataFragment parentFragment;
    RouteHistory currentRouteHistory;

    CircularProgressIndicator routeProgress;

    Button exitButton;
    TextView routText;
    Handler handler;
    ControllerAPI api;


    TextView canDriveTextView;
    ImageView aprovalImageView;
    TextView UserNametext;
    ShapeableImageView userImage;
    Button nextBtn;



    ReaderNFC readerNFC;

    public RouteFragment() {

    }

    @Override
    public void onStart() {
        this.api = RetrofitService.getApi();
        this.handlerThread = new HandlerThread("API Call Thread");
        this.handlerThread.start();

        scanInterractions = new ArrayList<ScanInterraction>();
        handler = new Handler(handlerThread.getLooper());


        handler.post(() -> {
            loadInitial();
            readerNFC = new ReaderNFC(handlerThread, getActivity());
            readerNFC.enableReaderMode();
            readerNFC.subscribeToIdRead(this);
        });



        exitButton = (Button) getView().findViewById(R.id.exitButton);
        routText = (TextView) getView().findViewById(R.id.routeText);
        canDriveTextView = (TextView) getView().findViewById(R.id.canDriveText);
        UserNametext = getView().findViewById(R.id.UserName);
        userImage = getView().findViewById(R.id.userImage);
        aprovalImageView = (ImageView) getView().findViewById(R.id.statusImage);
        routeProgress = (CircularProgressIndicator) getView().findViewById(R.id.progressRoute);
        nextBtn = (Button) getView().findViewById(R.id.nextBtn);


        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


                builder.setMessage("Da li žeilte da završite rad sa datim terminalom?")
                        .setTitle("Napusti terminal");
                builder.setPositiveButton("Ne", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                builder.setNegativeButton("Da", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, parentFragment).commit();
                    }
                });

                builder.show();


            }
        });


        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableNextUserToLoad();
            }
        });
        super.onStart();
    }

    private void enableNextUserToLoad() {
        nextBtn.setVisibility(View.INVISIBLE);
        userImage.setVisibility(View.INVISIBLE);
        canDriveTextView.setVisibility(View.INVISIBLE);
        aprovalImageView.setVisibility(View.INVISIBLE);
        UserNametext.setText("");
        readerNFC.enableReaderMode();

    }

    private void loadInitial() {
        getActivity().runOnUiThread(() -> {
            routeProgress.setVisibility(View.VISIBLE);
        });


        try {
            List<Route> routes = api.getAllRoutes(TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
            if(routes!=null) {
                for (Route route : routes)
                    if (route.getId() == currentRouteHistory.getPrimaryKey().getRouteId())
                        getActivity().runOnUiThread(() ->
                        {
                            routText.setText(route.getName());


                        });

                loadScanInteractions();
            }

        } catch (IOException e) {

        } finally {
            getActivity().runOnUiThread(() -> {
                routeProgress.setVisibility(View.INVISIBLE);
            });
        }


    }

    private void loadScanInteractions() throws IOException {
        List<ScanInterraction> gottenScans = api.getScanInteractions(currentRouteHistory.getPrimaryKey().getTerminalId(), 45L, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
        if (gottenScans != null && gottenScans.size() > 0)
            scanInterractions.addAll(gottenScans);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_route, container, false);
    }


    public void setRouteHistory(RouteHistory roteHistory) {
        this.currentRouteHistory = roteHistory;
    }

    public void setParentFragment(LoadTerminalDataFragment parentFragment) {
        this.parentFragment = parentFragment;
    }

    private void checkUser(String UserId) {


        User user = null;
        try {
            user = api.getUserById(UserId, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
        } catch (IOException e) {

        }
        byte[] pictureBytes = null;
        if (user != null && user.getPictureHash() != null) {

            try {
                pictureBytes = api.getUserProfilePicture(user.getId(), TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body().bytes();

            } catch (IOException e) {

            }
        }
        final User result = user;
        final byte[] pBytes = pictureBytes;
        final boolean canDrive;
        if (!scanInterractions.stream().anyMatch(s -> s.getId().getUserId() == result.getId()))
            try {
                loadScanInteractions();
            } catch (IOException e) {
            }
        canDrive = scanInterractions.stream().anyMatch(s -> s.getId().getUserId() == result.getId());
        getActivity().runOnUiThread(() ->
        {
            routeProgress.setVisibility(View.INVISIBLE);
            nextBtn.setVisibility(View.VISIBLE);

            if (result == null) {
                this.userNotFound();
            } else {
                userFound(result, pBytes, canDrive);
            }

        });
    }

    private void userFound(User result, byte[] pBytes, boolean canDrive) {
        UserNametext.setText(result.getFirstName() + " " + result.getLastName());

        if (pBytes != null) {
            //Found and has picture
            userImage.setImageBitmap(BitmapFactory.decodeByteArray(pBytes, 0, pBytes.length));
        } else {
            //Found doesnt have picture
            userImage.setImageDrawable(getResources().getDrawable(R.drawable.no_profile_picture));
            userImage.setVisibility(View.VISIBLE);
        }

        userImage.setVisibility(View.VISIBLE);
        canDriveTextView.setVisibility(View.VISIBLE);
        if (canDrive) {

            canDriveTextView.setTextColor(Color.GREEN);
            canDriveTextView.setText("Ima validnu kartu");

            aprovalImageView.setImageDrawable(getResources().getDrawable(R.drawable.aproved));

        } else {

            canDriveTextView.setTextColor(Color.RED);
            canDriveTextView.setText("Nema validnu kartu");

            aprovalImageView.setImageDrawable(getResources().getDrawable(R.drawable.rejected));

        }
        aprovalImageView.setVisibility(View.VISIBLE);
    }

    public void userNotFound() {
        UserNametext.setText("Korisnik nije pronađen");
        canDriveTextView.setVisibility(View.INVISIBLE);
        userImage.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onIdRead(Integer id) {
        readerNFC.disableReaderMode();
        if (id > 0)
            handler.post(() -> {

                checkUser("" + id);
            });
    }


    @Override
    public void onDestroy() {
        this.handlerThread.quit();
        readerNFC.disableReaderMode();
        super.onDestroy();
    }
}