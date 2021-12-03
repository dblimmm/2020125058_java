package com.example.study1;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

class MyClass
{
    public static boolean leftCheck = true;
}

public class MainActivity extends AppCompatActivity {
    private Button btn_test;
    private Button btn_move;
    private EditText et_id;
    private String str;
    private ImageView test_img;
    private ImageView u_cat;
    private ImageView btn_left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_id = findViewById(R.id.et_id);
        btn_test = findViewById(R.id.btn_test);
        btn_move = findViewById(R.id.btn_move);
        test_img = findViewById(R.id.test_img);
        u_cat = findViewById(R.id.imageView);
        btn_left = findViewById(R.id.btn_left);

        //버튼이 눌렸을 때 입력창의 텍스트값 변경되도록 함
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_id.setText("버튼이 눌렸어요");
            }
        });

        //move 버튼이 눌렸을 때 입력 값을 저장해서 보낸 후 다음 페이지로 이동
        btn_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class); //인자로 현재 액티비티와 이동할 액티비티
                str = et_id.getText().toString();

                intent.putExtra("str", str); //이동할 액티비티에 str을 쏩니다
                startActivity(intent); //이동 구문
            }
        });

        //가운데 안드로이드 이미지 눌렸을 때 팝업창 나오기, 고양이 움직이게 해보기
        test_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "눌렸을 때 위치를 바꾸게 하는 방법이 뭘까요...", Toast.LENGTH_SHORT).show();

                //오른쪽으로 쬐깐씩 움직이기 + 애니메이션
                float x = u_cat.getTranslationX();
                ObjectAnimator animation = ObjectAnimator.ofFloat(u_cat, "translationX", x + 50);//고정 좌표
                animation.setDuration(200);
                animation.start();

                //된다! get로케이션으로는 초기 설정 y값만 받아오는데, trans를 붙이면 현재 y값을 받아오는 듯.
                float y = u_cat.getTranslationY();
                u_cat.setTranslationY(y + 20);//고정 좌표

                //이미지를 바꿔요

                if(MyClass.leftCheck)
                {
                    btn_left.setImageResource(R.drawable.left_r);
                    MyClass.leftCheck = false;
                }
                else
                {
                    btn_left.setImageResource(R.drawable.left);
                    MyClass.leftCheck = true;
                }

            }
        });

    } //Oncreat종료
}//public MainActivity 종료
