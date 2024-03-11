package test.designe.app.controllerapp.fragment;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import test.designe.app.controllerapp.CaptureAct;
import test.designe.app.controllerapp.R;
import test.designe.app.controllerapp.nfc.ReaderNFC;
import test.designe.app.controllerapp.TokenManager;
import test.designe.app.controllerapp.models.Route;
import test.designe.app.controllerapp.models.RouteHistory;
import test.designe.app.controllerapp.models.ScanInterraction;
import test.designe.app.controllerapp.models.User;
import test.designe.app.controllerapp.nfc.UserStringReadSubscriber;
import test.designe.app.controllerapp.retrofit.ControllerAPI;
import test.designe.app.controllerapp.retrofit.RetrofitService;


public class RouteFragment extends Fragment implements UserStringReadSubscriber {

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

    TextView UserNametext;
    ShapeableImageView userImage;
    Button nextBtn;
    Button qrBtn;
    long time=15l;


    //User data

    User user = null;
    byte[] pictureBytes = null;


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
            readerNFC.subscribeToUserStringRead(this);
        });



        exitButton = (Button) getView().findViewById(R.id.exitButton);
        routText = (TextView) getView().findViewById(R.id.routeText);
        canDriveTextView = (TextView) getView().findViewById(R.id.canDriveText);
        UserNametext = getView().findViewById(R.id.UserName);
        userImage = getView().findViewById(R.id.userImage);
        routeProgress = (CircularProgressIndicator) getView().findViewById(R.id.progressRoute);
        nextBtn = (Button) getView().findViewById(R.id.nextBtn);
        qrBtn = (Button)  getView().findViewById(R.id.qr_button);
        Slider timeSlider = (Slider) getView().findViewById(R.id.slider);
        TextView timeText =  (TextView) getView().findViewById(R.id.timeText);

        timeSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                time = (long)timeSlider.getValue();
                timeText.setText(time+" min");
                if(user!=null && pictureBytes!=null)
                {
                    userDrivingUiUpdate(canUserDrive(user));
                }

            }
        });


        qrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCode();
            }
        });


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
                builder.setCancelable(false);
                builder.create().show();


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
        user=null;
        pictureBytes=null;
        nextBtn.setVisibility(View.GONE);
        userImage.setVisibility(View.INVISIBLE);
        qrBtn.setVisibility(View.VISIBLE);
        canDriveTextView.setVisibility(View.INVISIBLE);
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
        List<ScanInterraction> gottenScans = api.getScanInteractions(currentRouteHistory.getPrimaryKey().getTerminalId(), 120, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
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

    private void checkUser(String UserString) {

        String UserId = UserString.split("\\.")[0];

        user = null;
        pictureBytes = null;

        try {
            user = api.getUserById(UserId, TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body();
        } catch (IOException e) {

        }

        if (user != null && user.getPictureHash() != null) {

            try {
                pictureBytes = api.getUserProfilePicture(user.getId(), TokenManager.bearer() + TokenManager.getInstance().getToken()).execute().body().bytes();

            } catch (IOException e) {

            }
        }
        final User result = user;
        final byte[] pBytes = pictureBytes;
        final boolean canDrive;
        if (!canUserDrive(result))
            try {
                loadScanInteractions();
            } catch (IOException e) {
            }
        canDrive = canUserDrive(result);
        getActivity().runOnUiThread(() ->
        {
            qrBtn.setVisibility(View.GONE);
            routeProgress.setVisibility(View.INVISIBLE);
            nextBtn.setVisibility(View.VISIBLE);

            if (result == null) {
                this.userNotFound();
            } else {
                userFound(result, pBytes, canDrive);
            }

        });
    }

    private boolean canUserDrive(User user)
    {
        return scanInterractions.stream().filter(i->i.getId().getTime().after(new Timestamp(System.currentTimeMillis()-time*60*1000))).anyMatch(s -> s.getId().getUserId() == user.getId());
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
        userDrivingUiUpdate(canDrive);

    }

    private void userDrivingUiUpdate(boolean canDrive) {
        if (canDrive) {

            userCanDriveUiUpdate("#4bae4f", "Ima validnu kartu");


        } else {

            userCanDriveUiUpdate("#FF8E0409", "Nema validnu kartu");


        }
    }

    private void userCanDriveUiUpdate(String colorString, String Nema_validnu_kartu) {
        canDriveTextView.setTextColor(Color.parseColor(colorString));
        userImage.setStrokeColor(ColorStateList.valueOf(Color.parseColor(colorString)));
        canDriveTextView.setText(Nema_validnu_kartu);
    }


    public void userNotFound() {
        UserNametext.setText("Korisnik nije pronađen");
        canDriveTextView.setVisibility(View.INVISIBLE);
        userImage.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onOnUserStringReadRead(String UserString) {
        readerNFC.disableReaderMode();

            handler.post(() -> {

                checkUser(UserString);
            });
    }


    @Override
    public void onDestroy() {
        this.handlerThread.quit();
        readerNFC.disableReaderMode();
        super.onDestroy();
    }

    private void scanCode() {

        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);

        options.setCaptureActivity(CaptureAct.class);

        barLauncher.launch(options);



    }


    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult( new ScanContract() , result ->  {



        if(result.getContents()!=null)
        {
            handler.post(() -> {

                checkUser(result.getContents());
            });

        }
        else
        {
            userNotFound();
        }

    });
}