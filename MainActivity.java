package com.example.spookydoors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import java.util.*;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.example.spookydoors.Dice;
import com.example.spookydoors.ChaRacter;
import com.example.spookydoors.NounPlayerCharacter;
import com.example.spookydoors.PlayerCharacter;
import com.example.spookydoors.Door;


public class MainActivity extends AppCompatActivity
{
    //변수들 선언
    //아이디들 받아서 int arrays로 저장
    private int[] playerCIDs = {R.id.c1, R.id.c4};
    private int[] nonePlayerCIDs = {R.id.c2, R.id.c3};
    private int[] doorIDs = {R.id.door0, R.id.door1, R.id.door2, R.id.door3, R.id.door4, R.id.door5, R.id.door6, R.id.door7};
    //pc1, pc2, npc1, npc2순서로 이미지들(흐린 이미지, 일반 이미지 변환용)
    private int[] charactersOriImages = {R.drawable.c1, R.drawable.c4, R.drawable.c2, R.drawable.c3};
    private int[] charactersSoulImages = {R.drawable.c1_soul, R.drawable.c4_soul, R.drawable.c2_soul, R.drawable.c3_soul};
    //상단 누구 차례인지 표시하는 머리 이미지 변환용
    private int[] charactersHeadImages = {R.drawable.c1_head, R.drawable.c4_head};
    //ImageView객체들, pc, npc, door, head
    private ImageView[] playerCImgaes = new ImageView[2];
    private ImageView[] nonePlayerCImgaes = new ImageView[2];
    private ImageView[] doorImages = new ImageView[8];
    private ImageView head;
    private TextView status; //상단에 AP, Candy를 표시하는 Textview
    //ImageView객체들, 하단 버튼 3개
    private ImageView btnDice;
    private ImageView btnRight;
    private ImageView btnLeft;
    //private ImageView btnUseDoor;
    //지정 라운드 동안 thread가 계속되서 실행되도록 하는 boolean
    private boolean threadOn = true;
    //현재 위치의 문 확인하는 스레드, 캐릭터간 이것저것(턴 확인, npc이동 등 순차적) 확인하는 스레드
    private Thread checkCharacterThread;
    private String hereDoorShape = "String"; //현재 PC가 있는 위치의 문 모양을 저장한다.
    //어떤 플레이어의 턴인지 확인하는 것. 인덱스로 사용함. 0, 1만 가능.
    private int whosTrun = 0;

    //각 객체의 정보값을 저장하는리스트 선언
    private ArrayList<Door> doors = new ArrayList<Door>(); //맵에 깔린 Door 정보를 관리하는 리스트
    private ArrayList<PlayerCharacter> playerCharacters = new ArrayList<PlayerCharacter>();
    private ArrayList<NounPlayerCharacter> nounPlayerCharacters = new ArrayList<NounPlayerCharacter>();

    //캐릭터들 번호 받아서 이미지 변경, 이미지 리셋, 이미지 이동 등 처리하는 핸들러 선언
    class MyHandler extends Handler
    {
        //이미지 변경 (공격 당함 / 멀쩡)
        public void changeNpcImageToSoul(int i)
        {
            nonePlayerCImgaes[i].setImageResource(charactersSoulImages[i + 2]);
        }
        public void changePcImageToSoul(int i)
        {
            playerCImgaes[i].setImageResource(charactersSoulImages[i]);
        }
        public void resetNpcImage(int i)
        {
            nonePlayerCImgaes[i].setImageResource(charactersOriImages[i + 2]);
        }
        public void resetPcImage(int i)
        {
            playerCImgaes[i].setImageResource(charactersOriImages[i]);
        }

        //이미지 변경 (누구 턴인지 표시하는 head)
        public void headChange(int i)
        {
            switch (i)
            {
                case 0:
                    head.setImageResource(charactersHeadImages[0]);
                    break;
                case 1:
                    head.setImageResource(charactersHeadImages[1]);
                    break;
                default:
                    break;
            }
        }
        //상단 AP, Candy표시하는 부분 변경
        public void setTextView(int ap, int candy)
        {
            status.setText("AP : " + String.valueOf(ap) + ", CANDY : " + String.valueOf(candy));
        }
        //게임이 끝나고 결과 화면으로 이동시킨다
        public void goToEndingPage()
        {
            Intent intent = new Intent(MainActivity.this, EndingPage.class); //인자로 현재 액티비티와 이동할 액티비티
            String pc1Candy = String.valueOf(playerCharacters.get(0).getCandy());
            String pc2Candy = String.valueOf(playerCharacters.get(1).getCandy());

            intent.putExtra("pc1Candy", pc1Candy); //이동할 액티비티에 pc캔디값을 쏩니다
            intent.putExtra("pc2Candy", pc2Candy);
            startActivity(intent);
        }

        //npc 랜덤 이동, 이미지 이동
        public void npcRandomMove()
        {
            for (NounPlayerCharacter npc : nounPlayerCharacters)
            {
                npc.randomMove(); //정보값 이동시키고
                //x 좌표 계산
                float x = -390 + npc.getX() * 135;
                //x좌표 이동
                nonePlayerCImgaes[nounPlayerCharacters.indexOf(npc)].setTranslationX(x);

                //pc두명 돌면서 충돌 있는지 확인, 공격 관련
                for (PlayerCharacter pc : playerCharacters)
                {
                    if (npc.getX() == pc.getX() && npc.getY() == pc.getY() && pc.getCandy() > 0)
                    {
                        pc.attacked();
                        npc.earnCandy();
                    }
                }//pc두명 돌면서 npc와의 충돌이 있는지 확인하는 for문 끝

                ///npc는 다른 층에 있어 서로 부딪힐 일이 없으므로 npc와의 충돌은 체크하지 않음

                //npc의 한 턴이 끝나면 PC의 isItAttacked를 초기화해요.
                for (PlayerCharacter pc : playerCharacters) {
                    pc.resetAttacked();
                }

            }//npc두명 돌면서 랜덤 이동시키는 for문 끝
        }//npc랜덤 이동시키는 함수 끝
    }//MyHandler클래스 끝

    //핸들러 객체
    private MyHandler myHandler = new MyHandler();

    //캐릭터가 위치한 자리에 어떤 door이 있는지 체크하는 함수
    public void checkDoorHere()
    {
        int count = 0;

        for (Door door : doors)
        {
            //모든 문을 돌면서 pc랑 같은 위치에 있는 문의 모양값을 받습니다.
            if (door.getX() == playerCharacters.get(whosTrun).getX() && door.getY() == playerCharacters.get(whosTrun).getY())
            {
                hereDoorShape = door.getShape();
                //isHereDoor = true;
                //Log.e("door", "Here door shape is" + door.getShape());
                break;

            }
            else
            {
                count++;
            }
        }
        //같은 위치에 door이 없다면 문 모양 표시하는 속성에 None저장한다.
        if(count == 8)
        {
            hereDoorShape = "None";
        }

    }

    //캐릭터의 이동 후 다른 캐릭터와 부딪혔는지 확인하는 함수
    public void checkCrashCharacter()
    {
        for (NounPlayerCharacter npc : nounPlayerCharacters)
        {
            //같은 위치에 있으면서, npc에게 candy가 남아있으면서, npc가 같은 턴에 공격당한 적이 없다면
            if (playerCharacters.get(whosTrun).getX() == npc.getX() && playerCharacters.get(whosTrun).getY() == npc.getY() && npc.getCandy() > 0 && !npc.getIsItAttacked())
            {
                //npc 공격당하고 이미지 변경
                npc.attacked();
                myHandler.changeNpcImageToSoul(nounPlayerCharacters.indexOf(npc));
                //pc는 캔디를 얻어요
                playerCharacters.get(whosTrun).earnCandy(); //해당 함수 내에서 다음 굴림 기회가 있는지 계산 후 반영
            }// 같은 칸에 있는 npc리스트 확인하는 if문 끝
        }//캔디 먹는 거 확인용으로 npc리스트 도는 거 끝

        for (PlayerCharacter pc2 : playerCharacters)
        {//리스트에서 자신이 아닌 다른 pc캐릭터이며, 위치가 같으며, candy가 남아있으면서, 같은 턴에 공격당한 적이 없다면
            if (playerCharacters.get(whosTrun) != pc2 && playerCharacters.get(whosTrun).getX() == pc2.getX() && playerCharacters.get(whosTrun).getY() == pc2.getY()
                    && pc2.getCandy() > 0 && !pc2.getIsItAttacked())
            {
                //pc2어택당하고 이미지 변경
                pc2.attacked();
                myHandler.changePcImageToSoul(playerCharacters.indexOf(pc2));
                //pc는 캔디를 얻어요
                playerCharacters.get(whosTrun).earnCandy();
            }
        }//캔디 먹는거 확인용으로 pc2리스트 도는 거 끝
    }

    //@@@@@@밑으로 온 크리에이트@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    @Override
    protected void onCreate(Bundle savedInstanceState){
        //기존에 있던 코드 두 줄
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //PC, NPC, DOOR (정보값) 리스트에 추가. 초기 설정.
        playerCharacters.add(new PlayerCharacter(3, 0, "N"));
        playerCharacters.add(new PlayerCharacter(3, 3, "D"));

        nounPlayerCharacters.add(new NounPlayerCharacter(3, 1));
        nounPlayerCharacters.add(new NounPlayerCharacter(3, 2));

        doors.add(new Door(0, 0, "N"));
        doors.add(new Door(6, 0, "W"));
        doors.add(new Door(0, 1, "D"));
        doors.add(new Door(6, 1, "C"));
        doors.add(new Door(0, 2, "W"));
        doors.add(new Door(6, 2, "C"));
        doors.add(new Door(0, 3, "N"));
        doors.add(new Door(6, 3, "D"));

        //ImageView들을 arrays에 연결해둡니다
        for(int i = 0; i < 2; i++)
        {
            playerCImgaes[i] = findViewById(playerCIDs[i]);
            nonePlayerCImgaes[i] = findViewById(nonePlayerCIDs[i]);
        }
        for(int i = 0; i < 8; i++)
        {
            doorImages[i] = findViewById(doorIDs[i]);
        }
        //버튼들을 연결해둡니다
        btnDice = findViewById(R.id.btn_dice);
        btnLeft = findViewById(R.id.btn_left);
        btnRight = findViewById(R.id.btn_right);
        head = findViewById(R.id.head);
        status = findViewById(R.id.status);
        //btnUseDoor = findViewById(R.id.btn_usedoor);

        //주사위 버튼을 눌러 rollingdice합니다
        btnDice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //굴림 기회가 있는지는 캐릭터 클래스 내부 메소드에서 확인/수정함.
                playerCharacters.get(whosTrun).rollingDice();
            }
        });

        //오른쪽 버튼을 눌렀을 때
        btnRight.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playerCharacters.get(whosTrun).moveRight();
                //이동할 x값은 -390(가장 왼쪽 칸)에서 위치*135(한칸만큼) 더함
                //캐릭터가 갖고 있는 x속성값의 위치로 이동
                float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                //translationX는 처음 설정 위치로부터의 상대 수치가 되는데, 모든 PC는 같은 X좌표에서 시작하므로 고정 좌표라 생각해도 무방하다.
                ObjectAnimator animation = ObjectAnimator.ofFloat(playerCImgaes[whosTrun], "translationX", x);
                animation.setDuration(200); //몇초동안 애니메이션 일어날 건지
                animation.start(); //애니메이션 시작
                checkCrashCharacter(); //다른 캐릭터와의 충돌을 계산하고 이미지 변경 등을 수행한다.
            }
        });

        //왼쪽 버튼을 눌렀을 때
        btnLeft.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playerCharacters.get(whosTrun).moveLeft();
                float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                ObjectAnimator animation = ObjectAnimator.ofFloat(playerCImgaes[whosTrun], "translationX", x);

                animation.setDuration(200);
                animation.start();
                checkCrashCharacter();
            }
        });


        //각 문 버튼을 눌러 이동이 가능합니다
        doorImages[0].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //캐릭터가 위치한 곳에 어떤 모양의 Door이 있는지 확인합니다
                checkDoorHere();
                //현재 위치한 곳의 모양과 눌린 곳의 모양이 일치하면
                if (hereDoorShape.equals(doors.get(0).getShape()))
                {
                    //x, y 정보값 변경
                    playerCharacters.get(whosTrun).useDoor(doors.get(0).getX(), doors.get(0).getY());
                    //변경된 x, y토대로 좌표값 설정
                    float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                    float y = playerCharacters.get(whosTrun).getY() * 278;
                    //상대좌표기에 pc2의 경우 다른 방법으로 계산
                    if(whosTrun == 1)
                    {
                        y = -825 + playerCharacters.get(whosTrun).getY() * 278;
                    }
                    //설정된 좌표값으로 이미지 이동
                    playerCImgaes[whosTrun].setTranslationX(x);
                    playerCImgaes[whosTrun].setTranslationY(y);
                    //이동 후 다른 캐릭터와 부딪힘 확인
                    checkCrashCharacter();
                }
            }
        });

        doorImages[1].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //캐릭터가 위치한 곳의 doorShape와 누른 곳의 doorShape가 같으면
                checkDoorHere();
                if (hereDoorShape.equals(doors.get(1).getShape()))
                {
                    playerCharacters.get(whosTrun).useDoor(doors.get(1).getX(), doors.get(1).getY());
                    float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                    float y = playerCharacters.get(whosTrun).getY() * 278;
                    if(whosTrun == 1)
                    {
                        y = -825 + playerCharacters.get(whosTrun).getY() * 278;
                    }
                    playerCImgaes[whosTrun].setTranslationX(x);
                    playerCImgaes[whosTrun].setTranslationY(y);
                    checkCrashCharacter();
                }
            }
        });

        doorImages[2].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checkDoorHere();
                if (hereDoorShape.equals(doors.get(2).getShape()))
                {
                    //x, y 정보값 변경
                    playerCharacters.get(whosTrun).useDoor(doors.get(2).getX(), doors.get(2).getY());
                    float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                    float y = playerCharacters.get(whosTrun).getY() * 278;
                    if(whosTrun == 1)
                    {
                        y = -825 + playerCharacters.get(whosTrun).getY() * 278;
                    }
                    playerCImgaes[whosTrun].setTranslationX(x);
                    playerCImgaes[whosTrun].setTranslationY(y);
                    checkCrashCharacter();
                }
            }
        });

        doorImages[3].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //캐릭터가 위치한 곳의 doorShape와 누른 곳의 doorShape가 같으면
                checkDoorHere();
                if (hereDoorShape.equals(doors.get(3).getShape()))
                {
                    playerCharacters.get(whosTrun).useDoor(doors.get(3).getX(), doors.get(3).getY());
                    float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                    float y = playerCharacters.get(whosTrun).getY() * 278;
                    if(whosTrun == 1)
                    {
                        y = -825 + playerCharacters.get(whosTrun).getY() * 278;
                    }
                    playerCImgaes[whosTrun].setTranslationX(x);
                    playerCImgaes[whosTrun].setTranslationY(y);
                    checkCrashCharacter();
                }
            }
        });

        doorImages[4].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checkDoorHere();
                if (hereDoorShape.equals(doors.get(4).getShape()))
                {
                    playerCharacters.get(whosTrun).useDoor(doors.get(4).getX(), doors.get(4).getY());
                    float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                    float y = playerCharacters.get(whosTrun).getY() * 278;
                    if(whosTrun == 1)
                    {
                        y = -825 + playerCharacters.get(whosTrun).getY() * 278;
                    }
                    playerCImgaes[whosTrun].setTranslationX(x);
                    playerCImgaes[whosTrun].setTranslationY(y);
                    checkCrashCharacter();
                }
            }
        });

        doorImages[5].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //캐릭터가 위치한 곳의 doorShape와 누른 곳의 doorShape가 같으면
                checkDoorHere();
                if (hereDoorShape.equals(doors.get(5).getShape()))
                {
                    //x, y 정보값 변경
                    playerCharacters.get(whosTrun).useDoor(doors.get(5).getX(), doors.get(5).getY());
                    float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                    float y = playerCharacters.get(whosTrun).getY() * 278;
                    //상대좌표기에 pc2의 경우 다른 방법으로 계산
                    if(whosTrun == 1)
                    {
                        y = -825 + playerCharacters.get(whosTrun).getY() * 278;
                    }
                    //설정된 좌표값으로 이미지 이동
                    playerCImgaes[whosTrun].setTranslationX(x);
                    playerCImgaes[whosTrun].setTranslationY(y);
                    checkCrashCharacter();
                }
            }
        });

        doorImages[6].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //캐릭터가 위치한 곳의 doorShape와 누른 곳의 doorShape가 같으면
                checkDoorHere();
                if (hereDoorShape.equals(doors.get(6).getShape()))
                {
                    //x, y 정보값 변경
                    playerCharacters.get(whosTrun).useDoor(doors.get(6).getX(), doors.get(6).getY());
                    float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                    float y = playerCharacters.get(whosTrun).getY() * 278;
                    if(whosTrun == 1)
                    {
                        y = -900 + playerCharacters.get(whosTrun).getY() * 278;
                    }
                    //설정된 좌표값으로 이미지 이동
                    playerCImgaes[whosTrun].setTranslationX(x);
                    playerCImgaes[whosTrun].setTranslationY(y);
                    checkCrashCharacter();
                }
                //playerCharacters.get(whosTrun).testPrint();
                //Log.e("Door", "door[6] clicked");
                //Log.e("Door", "door[6]shape is " + doors.get(6).getShape());
                //Log.e("Door", "hereDoorShape is " + hereDoorShape);
            }
        });

        doorImages[7].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //캐릭터가 위치한 곳의 doorShape와 누른 곳의 doorShape가 같으면
                checkDoorHere();
                if (hereDoorShape.equals(doors.get(7).getShape()))
                {
                    //x, y 정보값 변경
                    playerCharacters.get(whosTrun).useDoor(doors.get(7).getX(), doors.get(7).getY());
                    float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                    float y = playerCharacters.get(whosTrun).getY() * 278;
                    //상대좌표기에 pc2의 경우 다른 방법으로 계산
                    if(whosTrun == 1)
                    {
                        y = -825 + playerCharacters.get(whosTrun).getY() * 278;
                    }
                    //설정된 좌표값으로 이미지 이동
                    playerCImgaes[whosTrun].setTranslationX(x);
                    playerCImgaes[whosTrun].setTranslationY(y);
                    checkCrashCharacter();
                }
            }
        });


        checkCharacterThread = new Thread()
        {
            public void run()
            {
                int round = 1;
                    //전체를 계속 진행합니다
                while (threadOn)
                {
                    Turn: //이건 한 턴 동안
                    while(threadOn)
                    {
                        myHandler.setTextView(playerCharacters.get(whosTrun).getActionPoint(), playerCharacters.get(whosTrun).getCandy());

                        //ap가 0이면서 굴림 기회를 모두 소진한 경우 나간다
                        if (playerCharacters.get(whosTrun).getActionPoint() == 0 && playerCharacters.get(whosTrun).getSecondRoll() == false &&
                                playerCharacters.get(whosTrun).getFistRoll() == false)
                        {
                            break Turn;
                        }
                        //계속 확인하고 있기에 sleep
                        try
                        {
                            sleep(50);
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }

                    }//한 pc의 액션포인트가 잔존하는 동안 계속 턴 갖는 거 끝
                    round++;
                    if(round == 10) //결과 화면 확인용으로 적은 수를 설정하였음.
                        {
                            //round가 10이라면 게임 결과 화면으로 이동한다.
                            //바로 이동하면 플레이어 입장에서 당황스러우므로 1초 대기 후 이동.
                            try
                            {
                                sleep(1000);
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                            myHandler.goToEndingPage();
                            break; //thread의 전체 while에서 벗어난다.
                        }
                    playerCharacters.get(whosTrun).resetSecondRolled(); //방금 pc 의 boolean들 전체 초기화

                    //차례 넘김
                    if(whosTrun == 0)
                    {
                        whosTrun = 1;
                    }else{
                        whosTrun = 0;
                    }
                    //상단 차례 표시 헤드 변경
                    myHandler.headChange(whosTrun);
                    //차례 변경 후 textView도 수정
                    myHandler.setTextView(playerCharacters.get(whosTrun).getActionPoint(), playerCharacters.get(whosTrun).getCandy());


                    //pc턴 종료마다 모든 캐릭터 공격당했는지 확인 하는 것 0으로 초기화, 이미지도 초기화
                    for (NounPlayerCharacter npc : nounPlayerCharacters)
                    {
                        npc.resetAttacked();
                        myHandler.resetNpcImage(nounPlayerCharacters.indexOf(npc));
                    }
                    for (PlayerCharacter pc2 : playerCharacters)
                    {
                        pc2.resetAttacked();
                        myHandler.resetPcImage(playerCharacters.indexOf(pc2));
                    }

                    //한 캐릭터 턴이 끝날 때 마다 npc 랜덤하게 이동함.
                    myHandler.npcRandomMove();

                }//threadOn인 동안 계속 도는 while 끝

            }//thread public void run(){}종료
        }; //thread = new Thread(){};종료

        checkCharacterThread.start(); //Thread 실행
    }//Oncreate 종료
}//메인 액티비티 종료
