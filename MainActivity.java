package com.example.spookydoors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import java.util.*;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

class Dice
{
    private static Random rollingNumber = new Random(); //주사위 객체를 만들어두어요

    //a면체 주사위를 굴려요 1~a
    public static int rollingDice(int a)
    {
        return rollingNumber.nextInt(a) + 1;
    }
}

class Door
{
    final private int xCoordinate;
    final private int yCoordinate;
    final private String shape;
    private boolean isItUsed; //이것은 '나오는 문'에 대한 속성이다. 나왔던 문이 회수 가능한 지 확인하는 것.

    //생성자
    //문의 쉐이프는 "C", "W", "N", "D"
    public Door(int x, int y, String shape)
    {
        xCoordinate = x;
        yCoordinate = y;
        this.shape = shape;
        isItUsed = false;
    }
    public void useIt()
    {
        isItUsed = true;
    }
    public void resetUse()
    {
        isItUsed = false;
    }


    //속성을 return하는 GET함수들
    public int getX()
    {
        return xCoordinate;
    }
    public int getY()
    {
        return yCoordinate;
    }
    public String getShape()
    {
        return shape;
    }
    public boolean getIsItUsed()
    {
        return isItUsed;
    }
    public void testPrint()
    {
        //System.out.printf("이 door은 (%d, %d)에 위치한 %s입니다.\n", xCoordinate, yCoordinate, shape);
        Log.e("testPrint door:", String.valueOf(xCoordinate) + String.valueOf(xCoordinate) + shape);
    }
}

//캐릭터에서 논플레이어캐릭터/플레이어캐릭터 하위 클래스를 가진다.
//Character이라는 기본 클래스가 있는 듯하여 중간에 하나 대문자로 함
class ChaRacter
{
    //모든 캐릭터는 x. y좌표가 있고 이동이 가능 함. 캔디를 가지고 있음.
    //플레이어 캐릭터는 문을 사용할 수 있음.
    //플레이어 캐릭터는 주사위를 굴릴 수 있음. AP(액션 포인트)를 갖고 있어야함.
    //상속하니까 private(X) protected(O)
    protected int xCoordinate; //x = 0~6 사이의 값을 가짐.
    protected int yCoordinate; //y = 0~3
    protected int candy; //소지 캔디 갯수
    protected boolean isItAttacked; //해당 라운드에 공격을 당했는지 안당했는지 표시하는 속성. 한 턴에 한번만 공격 당할 수 있다.

    //생성자
    public ChaRacter(int x, int y)
    {
        xCoordinate = x;
        yCoordinate = y;
        candy = 3; //기본 캔디는 항상 3개
        isItAttacked = false;
    }

    //이번 턴에 타인과 같은 칸에 위치하게 되어 공격 당했을 때
    public void attacked()
    {
        //아직 공격당한 적이 없으면서 캔디를 소지하고 있을 때
        if(!isItAttacked && candy > 0)
        {
            isItAttacked = true; //공격당했음을 표시
            candy --; //캔디 1개 감소
        }
    }

    public void earnCandy()
    {
        candy ++;
    }

    public void resetAttacked()
    {
        isItAttacked = false;
    }


    //속성을 return 하는 함수들
    public int getX()
    {
        return xCoordinate;
    }
    public int getY()
    {
        return yCoordinate;
    }
    public int getCandy()
    {
        return candy;
    }
    public boolean getIsItAttacked()
    {
        return isItAttacked;
    }
    public void testPrint()
    {
        //System.out.printf("이 캐릭터는 (%d, %d)에 위치하고 있으며, %d개의 캔디를 갖고 있습니다.\n", xCoordinate, yCoordinate, candy);
        Log.e("testPrint npc", String.valueOf(xCoordinate) + String.valueOf(yCoordinate) + String.valueOf(candy));
    }
}

class NounPlayerCharacter extends ChaRacter
{
    public NounPlayerCharacter(int x, int y)
    {
        super(x, y);
        //상위 클래스 생성자 candy = 3;정의한 것 등도 잘 들어간 것 확인하였음.
    }

    public void randomMove()
    {
        //x를 -1~1칸 랜덤하게 움직이도록 함.
        while(true)
        {//다른 pc가 있는 곳으로는 움직이지 않게 피해다니고 싶은데 어떻게 할 지 고민. ->피하지 않고 공격하는 게 나은 듯
            //3면체 주사위를 굴리고 2를 빼서 -1, 0, 1의 결과값
            int dice = (Dice.rollingDice(3) - 2);
            if ((xCoordinate + dice > 0) && (xCoordinate + dice < 6)) //결과값이 벽 뚫고 지나가지 않는다면
            {
                xCoordinate += dice; //이동 후 종료
                break;
            }
        }
    }

}

class PlayerCharacter extends ChaRacter
{
    private int actionPoint = 0;
    private ArrayList<Door> hasDoors; //갖고 있는 문의 종류에 대한 리스트
    //처음 생성할 때 한개의 문을 갖고 시작함.
    private boolean secondRoll; //자신의 턴에서 두번째 주사위 굴림 기회가 있는지(상대방의 사탕을 먹은 뒤) 체크함.
    //이것으로 한 턴에 여러 사람을 공격했을 때 매번 주사위를 굴리는 것을 방지.
    private boolean firstRoll; //초기에 주사위 여러번 굴리는 것 방지.
    //둘다 true일 때 굴림 기회가 있는걸로 하고 굴림 기회를 소진하면 false로 변경
    //근데 이렇게 하면 secondRoll을 여러번 할 수 있는 불상사가 생긴다!
    private boolean secondRolled; //두번째 주사위를 굴렸던 적이 있는지 따로 체크하자

    public PlayerCharacter(int x, int y, String doorShape) //생성자
    {
        super(x, y);
        secondRoll = false;
        firstRoll = true;
        secondRolled = false;
        hasDoors = new ArrayList<Door>();
        hasDoors.add(new Door(x, y, doorShape)); //x, y값은 아무렇게나 넣어서 리스트에 추가해둔다.
        //문을 생성할 때는 플레이어 캐릭터의 x,y값을 받아 새 문을 설치하고, 소지 문 목록에서 삭제하기 때문.
    }

    public void rollingDice() {
        if(firstRoll == true)
        {
            actionPoint = Dice.rollingDice(6); //액션 포인트 추가가 아니라 고정값으로 +=이 아니다.
            firstRoll = false;
        }
        else if(secondRoll == true && secondRolled == false) //2번째 굴림 기회를 얻었으면서 2번째 굴림을 한 적 없는 경우
        {
            actionPoint = Dice.rollingDice(6); //액션 포인트 추가가 아니라 고정값으로 +=이 아니다.
            secondRoll = false;
            secondRolled = true;
        }
        else
        {
            Log.e("dice error",  "굴림 기회가 없습니다.");
        }

    }

    public void moveRight() {
        if(actionPoint > 0) {
            if (xCoordinate != 6) //오른쪽 맨 끝 칸이 아닌 경우
            {
                xCoordinate++;
                actionPoint--;
            } else {
                //System.out.println("해당 방향으로 이동할 수 없습니다.");
                Log.e("move error", "해당 방향으로는 이동할 수 없습니다");
            }
        }else {
            Log.e("move error", "액션 포인트가 없어 이동할 수 없습니다.");
        }
    }

    public void moveLeft() {
        if(actionPoint > 0) {
            if (xCoordinate != 0) { //액션포인트는 main에서 체크하도록 하여서(액션포인트가 잔존한 한 턴을 이어간다) 좌표만 체크한다.
                xCoordinate--;
                actionPoint--;
            } else {
                //System.out.println("해당 방향으로 이동할 수 없습니다.");
                Log.e("move error", "해당 방향으로는 이동할 수 없습니다");
            }
        }else{
            Log.e("move error", "액션 포인트가 없어 이동할 수 없습니다.");
        }
    }

    public void useDoor(int x, int y) //문을 사용할 때 x,y를 받아서 한번에 이동한다.
    {
        if(actionPoint > 0)
        {
            //문을 사용할 때는 나왔던 문의 isitused 값이 변경되어야 한다.
            xCoordinate = x;
            yCoordinate = y;
            actionPoint--;
        }
    }

    //문을 회수하면서 갖고 있는 문에 하나를 추가한다.
    public void addDoor(int x, int y, String doorShape) {
        if(actionPoint > 0) {
            //문을 회수할 때는 main에 있는 전체 doors관리 array에서 해당 문이 삭제되어야 한다.
            hasDoors.add(new Door(x, y, doorShape));
            actionPoint--;
        }
    }

    //index값에 해당하는 위치의 Door를 지운다
    public void deleteDoor(int index) {
        if(actionPoint > 0) {
            hasDoors.remove(index);
            actionPoint--;
        }
    }

    public void earnCandy()
    {
        candy++;
        if(!secondRolled)
        {
            secondRoll = true; //캔디를 얻고 두번째 주사위 굴림 기회를 얻습니다!
            actionPoint = 0; //액션 포인트를 0으로 초기화 해버림
        }
    }

    public void resetSecondRolled() {
        secondRolled = false;
        secondRoll = false;
        firstRoll = true; //한 턴에 체크하는 전체 boolean 변수들 초기화
    }


    //소지하고 있는 Doors리스트를 반환하는 함수인데 이런게 되나? >됨.
    public ArrayList<Door> getHasDoors() {
        return hasDoors;
    }

    //밑으로 속성 리턴받는 함수들과 테스트 프린트 함수
    public int getActionPoint() {
        return actionPoint;
    }

    public boolean getSecondRoll() {
        return secondRoll;
    }
    public boolean getFistRoll(){return firstRoll;}

    public void testPrint()
    {
        /*
        //System.out.printf("이 캐릭터는 (%d, %d)에 위치하고 있으며, %d개의 캔디를 갖고 있습니다. 남은 AP는 %d입니다.\n", xCoordinate, yCoordinate, candy, actionPoint);
        Log.e("testPrint pc", String.valueOf(xCoordinate) + String.valueOf(yCoordinate) + String.valueOf(candy) + String.valueOf(actionPoint));
        for(Door door : hasDoors)
        {
            //System.out.printf("갖고 있는 door의 스트링은 : %s\n", door.getShape());
            Log.e("doorShape", door.getShape());
        }
         */
        Log.e("testPrint pc", "순서대로 x, y, candy, ap" + String.valueOf(xCoordinate) + String.valueOf(yCoordinate) + String.valueOf(candy) + String.valueOf(actionPoint));
    }
}


class MyHandlerMessages extends Handler
{
    //이게 되나 싶지만서도 심지어 위의 클래스에서 써야 하니까 ... ... 안될껄?... .. 각각에 또 context를 인자로 넣어야 하잖아
    /*
    public static void needRollMessage(Context context)
    {
        Toast.makeText(context, "AP가 없습니다. 주사위를 먼저 굴려주세요.", Toast.LENGTH_SHORT).show();
    }
    public static void cantMoveMessage(Context context)
    {
        Toast.makeText(context, "해당 방향으로는 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
    }*/
}

public class MainActivity extends AppCompatActivity
{
    //변수들 선언
    //아이디들
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
    private TextView status;
    //ImageView객체들, 하단 버튼 3개
    private ImageView btnDice;
    private ImageView btnRight;
    private ImageView btnLeft;
    //private ImageView btnUseDoor;
    //thread 계속해서 실행
    private boolean threadOn = true;
    //현재 위치의 문 확인하는 스레드, 캐릭터간 이것저것(턴 확인, npc이동 등 순차적) 확인하는 스레드
    private Thread checkCharacterThread;
    //private Thread checkDoorThread;
    //현재 위치에 문이 있는지 확인, 어떤 문인지 확인하는 용도의 변수(같은 문 사이에 이동 가능함)
    //private boolean isHereDoor; //이건 필요가 없네! door이 없을 때는 밑에 'none'을 넣으면 되니 구분 가능함.
    private String hereDoorShape = "String";
    //어떤 플레이어의 턴인지 확인하는 것. 인덱스로 사용함. 0, 1만 가능.
    private int whosTrun = 0;

    //각 리스트 선언
    private ArrayList<Door> doors = new ArrayList<Door>(); //맵에 깔린 Door 정보를 관리하는 리스트
    private ArrayList<PlayerCharacter> playerCharacters = new ArrayList<PlayerCharacter>();
    private ArrayList<NounPlayerCharacter> nounPlayerCharacters = new ArrayList<NounPlayerCharacter>();

    //캐릭터들 번호 받아서 이미지 변경, 이미지 리셋, 이미지 이동 등 처리하는 핸들러 선언
    class MyHandler extends Handler
    {
        //toast 메시지 출력
        /*
        public void handleMessage(@NonNull Message msg)
        {
            Toast.makeText(getApplicationContext(), "핸들러 작동 확인 합니다요.", Toast.LENGTH_SHORT).show();
        }
         */

        //고민지점 : 해당 메시지 출력 조건을 계산하는 것은 저어기 밖에 캐릭터 클래스에 있음.
        //그래서 핸들러를 밖에서 새로 정의하려 했더니 getApplicationContext가 뭔지 확인해보셔야 할 듯 하여요
        public void needRollMessage()
        {
            Toast.makeText(getApplicationContext(), "AP가 없습니다. 주사위를 먼저 굴려주세요.", Toast.LENGTH_SHORT).show();
        }
        public void cantMoveMessage()
        {
            Toast.makeText(getApplicationContext(), "해당 방향으로는 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

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
        public void setTextView(int ap, int candy)
        {
            status.setText("AP : " + String.valueOf(ap) + ", CANDY : " + String.valueOf(candy));
        }

        //npc랜덤 이동, 이미지 이동
        public void npcRandomMove()
        {
            for (NounPlayerCharacter npc : nounPlayerCharacters)
            {
                npc.randomMove(); //이동시키고
                //애니메이션
                float x = -390 + npc.getX() * 135;
                nonePlayerCImgaes[nounPlayerCharacters.indexOf(npc)].setTranslationX(x);
                //ObjectAnimator animation = ObjectAnimator.ofFloat(nonePlayerCImgaes[nounPlayerCharacters.indexOf(npc)], "translationX", x);//고정 좌표
                //animation.setDuration(200); //몇초동안 애니메이션 일어날 건지
                //animation.start(); //thread안에서 화면에 관여할 수 없음.
                //android.util.AndroidRuntimeException: Animators may only be run on Looper threads오류 발생

                //pc두명 돌면서 충돌 있는지 확인
                for (PlayerCharacter pc : playerCharacters)
                {
                    if (npc.getX() == pc.getX() && npc.getY() == pc.getY())
                    {
                        pc.attacked(); //이미지 변경은 안하도록 해요...
                        npc.earnCandy();
                    }
                }//pc두명 돌면서 npc와의 충돌이 있는지 확인하는 for문 끝

                //npc의 한 턴이 끝나면 isItAttacked를 초기화해요
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
        //"D"를 입력해 자신 위치의 문을 사용하는 경우
        //현재 위치한 곳에 있는 문의 정보를 받아서, (고민 지점 : 리스트에 있는 shape를 모두 확인해야 할텐데,...
        //for문으로 doors를 쭉 도는 것 밖에 방법이 없을까?)
        //그 문과 같은 모양의 문들의 정보를 받아서,
        //그 문으로 이동이 가능하게끔 함.
        //나온 문은 사용되었음을 확인하는 변수가 변경 됨.
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
        if(count == 8)
        {
            hereDoorShape = "None";
            //Log.e("door", "Here door shape is None");
            //isHereDoor = false;
        }

    }

    //캐릭터의 이동 후 다른 캐릭터와 부딪혔는지 확인하는 함수
    public void checkCrashCharacter()
    {
        for (NounPlayerCharacter npc : nounPlayerCharacters)
        {
            if (playerCharacters.get(whosTrun).getX() == npc.getX() && playerCharacters.get(whosTrun).getY() == npc.getY() && npc.getCandy() > 0 && !npc.getIsItAttacked())
            {
                //npc 어택당하고 이미지 변경
                npc.attacked();
                myHandler.changeNpcImageToSoul(nounPlayerCharacters.indexOf(npc));
                //pc는 캔디를 얻어요
                playerCharacters.get(whosTrun).earnCandy();
                //캔디 먹은 뒤에는 주사위를 다시 굴려야 함(본인 턴에 1회만 가능), earnCandy 내에서 boolean 값 수정
            }// 같은 칸에 있는 npc리스트 확인하는 if문 끝
        }//캔디 먹는 거 확인용으로 npc리스트 도는 거 끝
        for (PlayerCharacter pc2 : playerCharacters)
        {//리스트에서 자신이 아닌 다른 pc캐릭터이며 위치가 같은 경우
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
                //float x = playerCImgaes[0].getTranslationX();
                playerCharacters.get(whosTrun).moveRight();
                //이동할 x값은 -390(가장 왼쪽 칸)에서 위치*135(한칸만큼) 더함
                //캐릭터가 갖고 있는 x속성값의 위치로 이동
                float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                ObjectAnimator animation = ObjectAnimator.ofFloat(playerCImgaes[whosTrun], "translationX", x);//고정 좌표
                animation.setDuration(200); //몇초동안 애니메이션 일어날 건지
                animation.start();
                checkCrashCharacter();
                //playerCharacters.get(whosTrun).testPrint();
            }
        });

        //왼쪽 버튼을 눌렀을 때
        btnLeft.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //float x = playerCImgaes[0].getTranslationX();
                playerCharacters.get(whosTrun).moveLeft();
                float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                ObjectAnimator animation = ObjectAnimator.ofFloat(playerCImgaes[whosTrun], "translationX", x);//고정 좌표

                animation.setDuration(200); //몇초동안 애니메이션 일어날 건지
                animation.start();
                checkCrashCharacter();
                //x = playerCImgaes[0].getTranslationX();
                //Log.e("xWitch:", String.valueOf(x));
                //playerCharacters.get(whosTrun).testPrint();
            }
        });


        //각 문 버튼을 눌러 이동이 가능합니다
        //pc1이 0번 문에서 6번 문 이동 문제 없음 확인
        doorImages[0].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //캐릭터가 위치한 곳의 doorShape와 누른 곳의 doorShape가 같으면
                checkDoorHere();
                if (hereDoorShape.equals(doors.get(0).getShape()))
                {
                    //x, y 정보값 변경
                    playerCharacters.get(whosTrun).useDoor(doors.get(0).getX(), doors.get(0).getY());
                    //Log.e("used door,", "now pc X and Y is" + String.valueOf(playerCharacters.get(whosTrun).getX()) + String.valueOf(playerCharacters.get(whosTrun).getY()));
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
                    //x, y 정보값 변경
                    playerCharacters.get(whosTrun).useDoor(doors.get(1).getX(), doors.get(1).getY());
                    //Log.e("used door,", "now pc X and Y is" + String.valueOf(playerCharacters.get(whosTrun).getX()) + String.valueOf(playerCharacters.get(whosTrun).getY()));
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
                }
            }
        });

        doorImages[2].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //캐릭터가 위치한 곳의 doorShape와 누른 곳의 doorShape가 같으면
                checkDoorHere();
                if (hereDoorShape.equals(doors.get(2).getShape()))
                {
                    //x, y 정보값 변경
                    playerCharacters.get(whosTrun).useDoor(doors.get(2).getX(), doors.get(2).getY());
                    //Log.e("used door,", "now pc X and Y is" + String.valueOf(playerCharacters.get(whosTrun).getX()) + String.valueOf(playerCharacters.get(whosTrun).getY()));
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
                    //x, y 정보값 변경
                    playerCharacters.get(whosTrun).useDoor(doors.get(3).getX(), doors.get(3).getY());
                    //Log.e("used door,", "now pc X and Y is" + String.valueOf(playerCharacters.get(whosTrun).getX()) + String.valueOf(playerCharacters.get(whosTrun).getY()));
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
                }
            }
        });

        doorImages[4].setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //캐릭터가 위치한 곳의 doorShape와 누른 곳의 doorShape가 같으면
                checkDoorHere();
                if (hereDoorShape.equals(doors.get(4).getShape()))
                {
                    //x, y 정보값 변경
                    playerCharacters.get(whosTrun).useDoor(doors.get(4).getX(), doors.get(4).getY());
                    //Log.e("used door,", "now pc X and Y is" + String.valueOf(playerCharacters.get(whosTrun).getX()) + String.valueOf(playerCharacters.get(whosTrun).getY()));
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
                    //Log.e("used door,", "now pc X and Y is" + String.valueOf(playerCharacters.get(whosTrun).getX()) + String.valueOf(playerCharacters.get(whosTrun).getY()));
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
                    //Log.e("used door,", "now pc X and Y is" + String.valueOf(playerCharacters.get(whosTrun).getX()) + String.valueOf(playerCharacters.get(whosTrun).getY()));
                    //변경된 x, y토대로 좌표값 설정
                    float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                    float y = playerCharacters.get(whosTrun).getY() * 278;
                    if(whosTrun == 1)
                    {
                        y = -900 + playerCharacters.get(whosTrun).getY() * 278;
                    }
                    //설정된 좌표값으로 이미지 이동
                    playerCImgaes[whosTrun].setTranslationX(x);
                    playerCImgaes[whosTrun].setTranslationY(y);
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
                    //Log.e("used door,", "now pc X and Y is" + String.valueOf(playerCharacters.get(whosTrun).getX()) + String.valueOf(playerCharacters.get(whosTrun).getY()));
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
                }
            }
        });


        checkCharacterThread = new Thread()
        {
            public void run()
            {
                int round = 0;
                //전체를 계속 진행합니다
                    while (threadOn)
                    {
                        Turn: //이건 한 턴 동안
                        while(threadOn)
                        {
                            //door 관련 코드 일단 전체 삭제하였음.!!!! 캐릭터들의 : 턴 넘김, 공격, 주사위, npc이동부터 만들기.
                            //여기서 textview수정하면 되지 않을까
                            myHandler.setTextView(playerCharacters.get(whosTrun).getActionPoint(), playerCharacters.get(whosTrun).getCandy());

                            //ap가 0이면서 굴림 기회를 모두 소진한 경우 나간다
                            if (playerCharacters.get(whosTrun).getActionPoint() == 0 && playerCharacters.get(whosTrun).getSecondRoll() == false &&
                                    playerCharacters.get(whosTrun).getFistRoll() == false)
                            {
                                break Turn;
                            }

                            try
                            {
                                sleep(50);
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }//한 pc의 액션포인트가 잔존하는 동안 계속 턴 갖는 거 끝
                    round++;
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


                    //pc턴 종료마다 모든 문 사용했는지 확인 하는 것 0으로 초기화
                    /*
                    for (Door door : doors) {
                        door.resetUse();
                    }*/

                    //pc턴 종료마다 모든 캐릭터 공격당했는지 확인 하는 것 0으로 초기화, 이미지도 초기화
                    for (NounPlayerCharacter npc : nounPlayerCharacters) {
                        npc.resetAttacked();
                        myHandler.resetNpcImage(nounPlayerCharacters.indexOf(npc));
                    }
                    for (PlayerCharacter pc2 : playerCharacters) {
                        pc2.resetAttacked();
                        myHandler.resetPcImage(playerCharacters.indexOf(pc2));
                    }

                    //한 캐릭터 턴이 끝날 때 마다 npc랜덤하게 이동함.
                    myHandler.npcRandomMove();

                }//theadOn인동안 계속 도는 while 끝

            }//thead public void run(){}종료
        }; //thread = new Thead(){};종료

        checkCharacterThread.start();
        //Log.e("c2 Y : ", String.valueOf(nonePlayerCImgaes[0].getTranslationY()));
        //Log.e("c3 Y : ", String.valueOf(nonePlayerCImgaes[1].getTranslationY()));
        //handler.dispatchMessage(new Message());
    }//Oncreate 종료
}//메인 액티비티 종료
