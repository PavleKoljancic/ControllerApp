package test.designe.app.controllerapp.nfc;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;

public class ReaderNFC implements NfcAdapter.ReaderCallback{
    static byte [] aid = {(byte)0xF0,0x01,0x02,0x03,0x04,0x05,0x06};

    static byte []  selectCommand = {(byte)0x00, (byte)0xA4, (byte)0x04,(byte) 0x00,(byte) 0x07, (byte)0xF0,0x01,0x02,0x03,0x04,0x05,0x06,0x00};
    static byte[] commandApdu = {
            (byte) 0x00,  // CLA
            (byte) 0xB0,  // INS (Read Binary)
            (byte) 0x00,  // P1 (Offset High Byte)
            (byte) 0x00,  // P2 (Offset Low Byte)
            (byte) 0x04 };  // Le (Expected Length of Response Data)
    private NfcAdapter nfcAdapter;
    HandlerThread handlerThread;
    Activity parentActivity;

    HashSet<IdReadSubscriber> idReadSubscribers;

    public ReaderNFC(HandlerThread handlerThread, Activity activity)
    {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        this.parentActivity =activity;
        this.handlerThread = handlerThread;
        idReadSubscribers = new HashSet<IdReadSubscriber>(3);
    }
    @Override
    public void onTagDiscovered(Tag tag) {
        Handler handler = new Handler(handlerThread.getLooper());
        //READY
        handler.post(()->{
            //Pronadjen tag
            IsoDep isoDep = IsoDep.get(tag);
            if(!isoDep.isConnected())
            {
                try{    isoDep.connect();
                    //Konekcija uspostavljena
                } catch (IOException e )
                {

                    //Konekcija nije uspostavljena
                }
            }
            if(!isoDep.isConnected())
                return;
            isoDep.setTimeout(350);
            {
                byte[] res = new byte[0];
                try {


                    res = isoDep.transceive(selectCommand);
                    //Pronalazak Usera
                    if(res[0]==-112&&res[1]==0) {
                        res = isoDep.transceive(commandApdu);
                        isoDep.close();
                        if(res.length==4) {
                            int id = ByteBuffer.wrap(res).getInt();
                            synchronized (idReadSubscribers) {
                                idReadSubscribers.stream().parallel().forEach(sub -> sub.onIdRead(id));
                            }
                        }

                    }
                } catch (IOException e) {
                    //KONEKCIJA IZGUBLJENA
                }
            }});


    }

    public void enableReaderMode()
    {
        if(nfcAdapter!=null)


            nfcAdapter.enableReaderMode(this.parentActivity,
                    this,NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK ,null);




    }

    public  void disableReaderMode()
    {   if(nfcAdapter!=null)
        nfcAdapter.disableReaderMode(this.parentActivity);


    }

    public   void subscribeToIdRead(IdReadSubscriber sub)
    {
        synchronized (this.idReadSubscribers)
        {
            this.idReadSubscribers.add(sub);
        }
    }
    public   void unsubscribeToIdRead(IdReadSubscriber sub)
    {
        synchronized (this.idReadSubscribers)
        {
            this.idReadSubscribers.remove(sub);
        }
    }
}
