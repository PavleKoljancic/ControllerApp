package test.designe.app.controllerapp.nfc;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;

public class ControllerAppHostApduService extends HostApduService {

    private static HandlerThread informThread;
    private static HashSet<IdReadSubscriber> subscribers;

    static {
        informThread = new HandlerThread("HCE Inform Thread");
        informThread.start();
        subscribers = new HashSet<IdReadSubscriber>();
    }


    private static final String RESPONSE_SUCCESS = "9000";
    static byte[] aidBytes = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x11, 0x22, 0x33};
    private static byte[] selectCommand = new byte[]{
            (byte) 0x00, // CLA (Class)
            (byte) 0xA4, // INS (Instruction) for SELECT
            0x04,        // P1 (Parameter 1) - Select by DF Name
            0x00,        // P2 (Parameter 2)
            0x07, // LC (Length of data)
            // AID bytes
            (byte) 0xA0, 0x00, 0x00, 0x03, 0x11, 0x22, 0x33,
            0x00         // LE (Expected response length)
    };

    private static byte[] write4BytesCommand = {(byte) 0x00, // CLA (Class)
            (byte) 0xD6, // INS (Instruction) for WRITE BINARY
            0x00,        // P1 (Parameter 1)
            0x00,        // P2 (Parameter 2)
            0x04};

    ByteBuffer byteBuffer = ByteBuffer.allocate(4);

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        if (Arrays.equals(commandApdu, selectCommand))
            return hexStringToByteArray(RESPONSE_SUCCESS);
        else {
            boolean write = true;
            for (int i = 0; write && i < write4BytesCommand.length; i++)
                write = (write4BytesCommand[i] == commandApdu[i]);

            if (write) {
                final byte[] dataSent = commandApdu;
                Handler handler = new Handler(informThread.getLooper());
                handler.post(() -> {
                    byte[] dataRead = new byte[4];
                    for (int i = write4BytesCommand.length; i < dataSent.length; i++)
                        dataRead[i - write4BytesCommand.length] = commandApdu[i];
                    Integer terminalId = ByteBuffer.wrap(dataRead).getInt();
                    if (terminalId > 0)
                        synchronized (subscribers) {
                            for (IdReadSubscriber subscriber : subscribers)
                                subscriber.onIdRead(terminalId);
                        }

                });


                return hexStringToByteArray(RESPONSE_SUCCESS);
            } else return hexStringToByteArray("6700");

        }
    }

    @Override
    public void onDeactivated(int reason) {
        // Not needed for this example
    }

    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    public static boolean subscribeToIdRead(IdReadSubscriber subscriber) {
        synchronized (subscribers) {
            return subscribers.add(subscriber);
        }
    }

    public static boolean unsubscribeToIdRead(IdReadSubscriber subscriber) {
        synchronized (subscribers) {
            return subscribers.remove(subscriber);
        }
    }
}
