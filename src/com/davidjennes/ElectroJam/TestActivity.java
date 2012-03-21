package com.davidjennes.ElectroJam;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

public class TestActivity extends Activity {
    
    private SoundManager mSoundManager;
    
    private int timeCounter = 0;

    Timer mtimer; 
    
    ToggleButton[] buttonTab;
    
    private String[] urlSons = new String[24];
    
    private int[] buttonState = new int [25];
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mtimer = new Timer();
        mtimer.scheduleAtFixedRate(new Action(), 0, 3750);
        
        timeCounter=0;
        
        mSoundManager = new SoundManager();
        mSoundManager.initSounds(getBaseContext());
        
        mSoundManager.addSound(1, R.raw.bassdrumogg);
        mSoundManager.addSound(2, R.raw.warpdrumogg);
        mSoundManager.addSound(3, R.raw.filterdrumogg);
        mSoundManager.addSound(4, R.raw.bassdrumchorusogg);
        
        mSoundManager.addSound(5, R.raw.hithatdelayogg);
        mSoundManager.addSound(6, R.raw.hithatfuzzogg);
        mSoundManager.addSound(7, R.raw.snareclapogg);
        mSoundManager.addSound(8, R.raw.snareclapreverbogg);
        
        buttonTab = new ToggleButton [25];
        
        buttonTab[1] = (ToggleButton) findViewById(R.id.ToggleButton1);
        buttonTab[2] = (ToggleButton) findViewById(R.id.ToggleButton2);
        buttonTab[3] = (ToggleButton) findViewById(R.id.ToggleButton3);
        buttonTab[4] = (ToggleButton) findViewById(R.id.ToggleButton4);
        buttonTab[5] = (ToggleButton) findViewById(R.id.ToggleButton5);
        buttonTab[6] = (ToggleButton) findViewById(R.id.ToggleButton6);
        buttonTab[7] = (ToggleButton) findViewById(R.id.ToggleButton7);
        buttonTab[8] = (ToggleButton) findViewById(R.id.ToggleButton8);
        buttonTab[9] = (ToggleButton) findViewById(R.id.ToggleButton9);
        buttonTab[10] = (ToggleButton) findViewById(R.id.ToggleButton10);       
        buttonTab[11] = (ToggleButton) findViewById(R.id.ToggleButton11);
        buttonTab[12] = (ToggleButton) findViewById(R.id.ToggleButton12);
        buttonTab[13] = (ToggleButton) findViewById(R.id.ToggleButton13);
        buttonTab[14] = (ToggleButton) findViewById(R.id.ToggleButton14);
        buttonTab[15] = (ToggleButton) findViewById(R.id.ToggleButton15);
        buttonTab[16] = (ToggleButton) findViewById(R.id.ToggleButton16);
        buttonTab[17] = (ToggleButton) findViewById(R.id.ToggleButton17);
        buttonTab[18] = (ToggleButton) findViewById(R.id.ToggleButton18);
        buttonTab[19] = (ToggleButton) findViewById(R.id.ToggleButton19);
        buttonTab[20] = (ToggleButton) findViewById(R.id.ToggleButton20);
        buttonTab[21] = (ToggleButton) findViewById(R.id.ToggleButton21);
        buttonTab[22] = (ToggleButton) findViewById(R.id.ToggleButton22);
        buttonTab[23] = (ToggleButton) findViewById(R.id.ToggleButton23);
        buttonTab[24] = (ToggleButton) findViewById(R.id.ToggleButton24);
        
        for (int i=1;i<25;i++) {
        	buttonState[i]=0;
        	buttonTab[i].setText("");
        	buttonTab[i].setTextOn("");
        	buttonTab[i].setTextOff("");
        }
        
        
        buttonTab[1].setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("1");
        	}
        });
        buttonTab[2].setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("2");
        	}
        });
        buttonTab[3].setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("3");
        	}
        });
        buttonTab[4].setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("4");
        	}
        });
        buttonTab[5].setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("5");
        	}
        });
        buttonTab[6].setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("6");
        	}
        });
        buttonTab[7].setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("7");
        	}
        });
        buttonTab[8].setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("8");
        	}
        });
        buttonTab[9].setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("9");
        	}
        });
        buttonTab[10].setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("10");
        	}
        });
        /*button11.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("11");
        	}
        });
        button12.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("12");
        	}
        });
        button13.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("13");
        	}
        });
        button14.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("14");
        	}
        });
        button15.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("15");
        	}
        });
        button16.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("16");
        	}
        });
        button17.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("17");
        	}
        });
        button18.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("18");
        	}
        });
        button19.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("19");
        	}
        });
        button20.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("20");
        	}
        });
        button21.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("21");
        	}
        });
        button22.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("22");
        	}
        });
        button23.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("23");
        	}
        });
        button24.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		buttonClick("24");
        	}
        }); */
        
        
            for (int j = 0; j < 3; j++) {
              this.urlSons[j] = "Son"+j+".wav";
            }

            
            for (int j = 0; j < 24; j++) {
                this.buttonState[j] = 0;
              }
        }
    
    public void onDestroy()
    {
        super.onDestroy();
        
        mtimer.cancel();
    }
    
    class Action extends TimerTask {
        public void run() {
            TestActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    timeCounter=timeCounter+1;
                    
                    for (int i = 1; i < 25 ; ++i) {
                    	if (buttonState[i]==1) {
                    		mSoundManager.stopSound(i);
                    		mSoundManager.playSound(i);
                    	}
                    }
                }
            });
        }
    }

    
    public void buttonClick(String str) {
    	
    	int i = Integer.parseInt(str);
		
    	if (buttonState[i] == 0 || buttonState[i] == 2)
    		buttonState[i]=1;
    	else if (buttonState[i] == 1) {
    		mSoundManager.stopSound(i);
        	buttonState[i]=2;
    	}
    }
}
