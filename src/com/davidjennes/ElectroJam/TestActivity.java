package com.davidjennes.ElectroJam;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

public class TestActivity extends Activity {
	private SoundManager m_soundManager;
	private Map<Integer, Integer> m_buttonSound;
    
    public TestActivity() {
        m_buttonSound = new HashMap<Integer, Integer>();
        m_buttonSound.put(R.id.LooperDrum1, R.raw.drum1);
        m_buttonSound.put(R.id.LooperDrum2, R.raw.drum2);
        m_buttonSound.put(R.id.LooperDrum3, R.raw.drum3);
        m_buttonSound.put(R.id.LooperDrum4, R.raw.drum4);
        m_buttonSound.put(R.id.LooperSnare1, R.raw.snare1);
        m_buttonSound.put(R.id.LooperSnare2, R.raw.snare2);
        m_buttonSound.put(R.id.LooperSnare3, R.raw.snare3);
        m_buttonSound.put(R.id.LooperSnare4, R.raw.snare4);
        m_buttonSound.put(R.id.LooperBass1, R.raw.bass1);
        m_buttonSound.put(R.id.LooperBass2, R.raw.bass1);
        m_buttonSound.put(R.id.LooperBass3, R.raw.bass1);
        m_buttonSound.put(R.id.LooperBass4, R.raw.bass1);
        m_buttonSound.put(R.id.LooperRythmic1, R.raw.bass1);
        m_buttonSound.put(R.id.LooperRythmic2, R.raw.bass1);
        m_buttonSound.put(R.id.LooperRythmic3, R.raw.bass1);
        m_buttonSound.put(R.id.LooperRythmic4, R.raw.bass1);
        m_buttonSound.put(R.id.LooperLead1, R.raw.bass1);
        m_buttonSound.put(R.id.LooperLead2, R.raw.bass1);
        m_buttonSound.put(R.id.LooperLead3, R.raw.bass1);
        m_buttonSound.put(R.id.LooperLead4, R.raw.bass1);
        m_buttonSound.put(R.id.LooperFX1, R.raw.bass1);
        m_buttonSound.put(R.id.LooperFX2, R.raw.bass1);
        m_buttonSound.put(R.id.LooperFX3, R.raw.bass1);
        m_buttonSound.put(R.id.LooperFX4, R.raw.bass1);
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instrument_looper);
        
        // initialize and load sounds
        m_soundManager = new SoundManager(getBaseContext());
        for (Map.Entry<Integer, Integer> entry : m_buttonSound.entrySet())
        	entry.setValue(m_soundManager.loadSound(entry.getValue()));
    }
    
    public void onDestroy() {
        super.onDestroy();
        
        for (Map.Entry<Integer, Integer> entry : m_buttonSound.entrySet())
    		m_soundManager.unloadSound(entry.getValue());
    }
    
    /**
     * Called on click of one of the buttons
     * @param view The clicked button
     */
    public void buttonClick(View view) {
    	if (view == null)
    		return;
    	
    	ToggleButton button = (ToggleButton) view;
    	
    	// play or stop
    	if (button.isChecked())
    		m_soundManager.playSound(m_buttonSound.get(button.getId()), true);
    	else
    		m_soundManager.stopSound(m_buttonSound.get(button.getId()));
    }
}
