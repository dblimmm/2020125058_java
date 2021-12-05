package com.example.spookydoors;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import java.util.*;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
    private int actionPoint;
    private ArrayList<Door> hasDoors; //갖고 있는 문의 종류에 대한 리스트
    //처음 생성할 때 한개의 문을 갖고 시작함.
    private boolean secondRoll; //자신의 턴에서 두번째 주사위 굴림 기회가 있는지(상대방의 사탕을 먹은 뒤) 체크함.
    //이것으로 한 턴에 여러 사람을 공격했을 때 매번 주사위를 굴리는 것을 방지.

    public PlayerCharacter(int x, int y, String doorShape) //생성자
    {
        super(x, y);
        secondRoll = false;
        hasDoors = new ArrayList<Door>();
        hasDoors.add(new Door(x, y, doorShape)); //x, y값은 아무렇게나 넣어서 리스트에 추가해둔다.
        //문을 생성할 때는 플레이어 캐릭터의 x,y값을 받아 새 문을 설치하고, 소지 문 목록에서 삭제하기 때문.
    }

    public void rollingDice() {
        actionPoint = Dice.rollingDice(6); //액션 포인트 추가가 아니라 고정값으로 +=이 아니다.
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
        if(actionPoint > 0) {
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

    public void secondRolled() {
        secondRoll = true;
    }

    public void resetSecondRoll() {
        secondRoll = false;
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

    public void testPrint()
    {
        //System.out.printf("이 캐릭터는 (%d, %d)에 위치하고 있으며, %d개의 캔디를 갖고 있습니다. 남은 AP는 %d입니다.\n", xCoordinate, yCoordinate, candy, actionPoint);
        Log.e("testPrint pc", String.valueOf(xCoordinate) + String.valueOf(yCoordinate) + String.valueOf(candy) + String.valueOf(actionPoint));
        for(Door door : hasDoors)
        {
            //System.out.printf("갖고 있는 door의 스트링은 : %s\n", door.getShape());
            Log.e("doorShape", door.getShape());
        }
    }
}


public class MainActivity extends AppCompatActivity
{
    //변수들 선언
    private int[] playerCIDs = {R.id.c1, R.id.c4};
    private int[] nonePlayerCIDs = {R.id.c2, R.id.c3};
    //door들은 어떻게 관리할 지 생각해야 함
    private ImageView[] playerCImgaes = new ImageView[2];
    private ImageView[] nonePlayerCImgaes = new ImageView[2];

    private ImageView btnDice;
    private ImageView btnRight;
    private ImageView btnLeft;

    private int whosTrun = 0; //어떤 플레이어의 턴인지 확인하는 것것

   //아하!!! 이 안에서 반복이 아니라 정의해두는거구나 이거 누르면 이렇게 움직이라고!!!!!!!!!
    //그래서 이 안에서 반복문 쓰면 안됨 화면이 안뜸 이거는 무족건 실행이 바로 완료되어야 함
    //아니 그럼 내 반복문은 어디에 쓰란 말이냐아~~? ->스레드와 핸들을 이용하자 !!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //기존에 있던 코드 두 줄
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ImageView들을 arrays에 연결해둡니다
        for(int i = 0; i < 2; i++)
        {
            playerCImgaes[i] = findViewById(playerCIDs[i]);
            nonePlayerCImgaes[i] = findViewById(nonePlayerCIDs[i]);
        }
        //버튼들을 연결해둡니다
        btnDice = findViewById(R.id.btn_dice);
        btnLeft = findViewById(R.id.btn_left);
        btnRight = findViewById(R.id.btn_right);

        //클래스
        //각 리스트 선언
        //ArrayList<Door> doors = new ArrayList<Door>(); //맵에 깔린 Door를 관리하는 리스트
        ArrayList<PlayerCharacter> playerCharacters = new ArrayList<PlayerCharacter>();
        ArrayList<NounPlayerCharacter> nounPlayerCharacters = new ArrayList<NounPlayerCharacter>();

        //PC, NPC 리스트에 추가. 초기 설정.
        playerCharacters.add(new PlayerCharacter(3, 0, "N"));
        playerCharacters.add(new PlayerCharacter(3, 3, "D"));

        nounPlayerCharacters.add(new NounPlayerCharacter(3, 1));
        nounPlayerCharacters.add(new NounPlayerCharacter(3, 2));


        btnRight.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //float x = playerCImgaes[0].getTranslationX();

                playerCharacters.get(whosTrun).moveRight();
                //이동할 x값은 -390(가장 왼쪽 칸)에서 위치*135(한칸만큼) 더함
                float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                ObjectAnimator animation = ObjectAnimator.ofFloat(playerCImgaes[whosTrun], "translationX", x);//고정 좌표
                animation.setDuration(200); //몇초동안 애니메이션 일어날 건지
                animation.start();
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //float x = playerCImgaes[0].getTranslationX();
                playerCharacters.get(whosTrun).moveLeft();
                float x = -390 + playerCharacters.get(whosTrun).getX() * 135;
                ObjectAnimator animation = ObjectAnimator.ofFloat(playerCImgaes[whosTrun], "translationX", x);//고정 좌표

                animation.setDuration(200); //몇초동안 애니메이션 일어날 건지
                animation.start();
                //x = playerCImgaes[0].getTranslationX();
                //Log.e("xWitch:", String.valueOf(x));
            }
        });
        //주사위 버튼이 눌러 rollingdice합니다
        btnDice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playerCImgaes[0].setImageResource(R.drawable.c1_soul);
                playerCharacters.get(whosTrun).rollingDice();
            }
        });

    }//Oncreate 종료
    
}
