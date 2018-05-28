package com.plane.plane;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.plane.plane.*;
/**
 * 主Activity
 */

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(viewId == R.id.btnGame){
            startGame();
        }
        else if (viewId==R.id.btnInstruction){
            lookInstruction();
        }
    }

    public void startGame() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
    public void lookInstruction(){
        Intent intent = new Intent(this, InstructionActivity.class);
        startActivity(intent);
    }
}
