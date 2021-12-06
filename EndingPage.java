package com.example.spookydoors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class EndingPage extends AppCompatActivity
{
    private Button btnRestart;
    private TextView result;
    private TextView winner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ending_page);

        btnRestart = findViewById(R.id.btn_restart);
        result = findViewById(R.id.result);
        winner = findViewById(R.id.winner);

        Intent intent = getIntent();
        String pc1Candy = intent.getStringExtra("pc1Candy");
        String pc2Candy = intent.getStringExtra("pc2Candy");

        result.setText("PC1 has " + pc1Candy + "candy, PC2 has " + pc2Candy +"candy");

        if(Integer.parseInt(pc1Candy) > Integer.parseInt(pc2Candy))
        {
            winner.setText("So winner is PC1!");
        }
        else if(Integer.parseInt(pc1Candy) < Integer.parseInt(pc2Candy))
        {
            winner.setText("So winner is PC2!");
        }
        else
        {
            winner.setText("DRAW ...");
        }

        //버튼을 누르면 mainActivity로 이동
        btnRestart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intentRetrun = new Intent(EndingPage.this, MainActivity.class); //인자로 현재 액티비티와 이동할 액티비티
                startActivity(intentRetrun); //이동 구문
            }
        });


    }
}
