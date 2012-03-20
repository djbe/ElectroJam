package com.davidjennes.ElectroJam;

import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class InstrumentService extends Service {
	private static final String TAG = "InstrumentService";
	
	@Override
    public void onCreate() {
        super.onCreate();
    }
	
	@Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }
	
	private final IInstrumentService.Stub m_binder = new IInstrumentService.Stub() {
		@Override
		public void loadSamples(Map samples) throws RemoteException {
			Log.d(TAG, "loading samples");
		}

		@Override
		public void sendEvent(String sample, int mode) throws RemoteException {
			Log.d(TAG, "playing sample: " + sample + " mode: " + mode);
		}
	};
}
