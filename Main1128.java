//기존 코드 맨 밑으로 내려두고 기말 프로젝트 코드 작성 중

import java.util.*;

//주사위 굴리는 용도의 클래스
class Dice
{
	private static Random rollingNumber = new Random(); //주사위 객체를 만들어두어요
	
	//a면체 주사위를 굴려요
	public static int rollingDice(int a)
	{
		return rollingNumber.nextInt(a) + 1;
	}
	
	public static String rollingDoor()
	{
		int dice = rollingNumber.nextInt(4); //0~3
		switch(dice)
		{
			case 0:
				return "C";
			case 1:
				return "W";
			case 2:
				return "N";
			case 3:
				return "D";
			default:
				return "error";
		}
	}
}


class Door
{
	//door는? 생성되거나 제거될 수 있다. (좌표값)이동은 불가하다. 사용시 다른 문과 연결.
	//그럼 x,y값은 final로 줘도 되겠네
	final private int xCoordinate;
	final private int yCoordinate;
	final private String shape;
	private int isItUsed; //이것은 '나오는 문'에 대한 속성임!!! 나왔던 문을 회수하는 거니까!!!
	
	//생성자
	//무슨 종류의 문인지?(4종) 추가해야겠네
	//문의 쉐이프는 "C", "W", "N", "D" 
	public Door(int x, int y, String shape)
	{
		xCoordinate = x;
		yCoordinate = y;
		this.shape = shape;
		isItUsed = 0;
	}
	public void useIt()
	{
		isItUsed = 1;
	}
	public void resetUse()
	{
		isItUsed = 0;
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
	public int getisItUsed()
	{
		return isItUsed;
	}
	public void testPrint()
	{
		System.out.printf("이 door은 (%d, %d)에 위치한 %s입니다.\n", xCoordinate, yCoordinate, shape);
	}
}

//캐릭터에서 논플레이어캐릭터/플레이어캐릭터 상속받으면 될 듯
//Character이라는 기본 클래스가 있는 듯? 중간에 하나 대문자로 함
class ChaRacter
{
	//모든 캐릭터는 x. y좌표가 있고 이동이 가능 함. 캔디를 가지고 있음.
	//플레이어 캐릭터는 문을 사용할 수 있음.
	//플레이어 캐릭터는 주사위를 굴릴 수 있음. AP(액션 포인트)를 갖고 있어야함.
	//상속하니까 private이면 안되지! protected
	protected int xCoordinate; //x = 0~6
	protected int yCoordinate; //y = 0~3
	protected int candy; //소지 캔디 갯수
	protected int isItAttacked; //전체 캐릭터는 해당 라운드에 공격을 당했는지 안당했는지 표시하는 속성이 있어야 함
	
	//생성자
	public ChaRacter(int x, int y)
	{
		xCoordinate = x;
		yCoordinate = y;
		isItAttacked = 0;
		candy = 3;
	}

	//이번 턴에 타인과 같은 칸에 위치하게 되면(공격)
	public void attacked()
	{
		if(isItAttacked ==0)
		{
			isItAttacked = 1; //공격당했음을 표시
			candy --; //캔디 1개 감소
		}
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
	public void testPrint()
	{
		System.out.printf("이 캐릭터는 (%d, %d)에 위치하고 있으며, %d개의 캔디를 갖고 있습니다.\n", xCoordinate, yCoordinate, candy);
	}
}

class NounPlayerCharacter extends ChaRacter
{
	public NounPlayerCharacter(int x, int y)
	{
		super(x, y);
	}
	
	public void randomMove()
	{
		//-1, 0, 1의 결과값이 나오려면 2면체 주사위를 굴려서 1을 빼면 되겠구나~
		while(true)
		{
			//범위 안에서 움직이도록 해요!
			int dice = (Dice.rollingDice(2) - 1);
			if ((xCoordinate + dice > 0) && (xCoordinate + dice < 6))
			{
				xCoordinate += dice;
				break;
			}
		}
	}
	
}

class PlayerCharacter extends ChaRacter
{
	private int actionPoint;
	private ArrayList<Door> hasDoors; //갖고 있는 문의 종류에 대한 리스트를 갖고 있어야 함. 처음 생성할 때 한개의 문을 갖고 시작함.
	private int secondRoll; //자신의 턴에서 두번째 주사위 굴림 기회가 있는지(상대방의 사탕을 먹은 뒤) 체크함.
	//두번째 굴림 기회에서 또 다른 캐릭터의 사탕을 먹는다면 어떻게 될까? 그때는 두번째 주사위 굴림 기회를 사용하면 안됨!
	//언제 0으로 초기화해야할지 고민해 봐야 함.
	
	//플레이어 캐릭터는 행동 시 AP가 깎이는 것을 여차저차 만들어야 함
	//근데 문이 '사용된'문인걸 확인하는 게 나을 것 같음. !! 왜냐하면 나왔던 문만 회수할 수 있는거니까!
	
	public PlayerCharacter(int x, int y, String doorShape) //생성자
	{
		super(x, y);
		secondRoll = 0;
		hasDoors = new ArrayList<Door>();
		hasDoors.add(new Door(x, y, doorShape));
	}
	
	public void rollingDice()
	{
		actionPoint = Dice.rollingDice(6); //추가가 아니라 고정값임! +=아님!
	}
	
	public void moveRight()
	{
		if(actionPoint > 0 && xCoordinate != 6) //액션포인트가 있으면서 오른쪽 맨 끝 칸이 아닌 경우
		{
			xCoordinate ++;
			actionPoint --;
		}
	}
	
	public void moveLeft()
	{
		if(actionPoint > 0 && xCoordinate != 0) //액션포인트가 있으면서 왼쪽 맨 끝 칸이 아닌 경우
		{
			xCoordinate --;
			actionPoint --;
		}
	}
	
	public void useDoor(int x, int y) //문을 사용할 때 x,y를 받아서 워프해요
	{
		//문을 사용할 때는 나왔던 문의 isitused 값이 변경되어야 한다.
		if(actionPoint > 0)
		{
			xCoordinate = x;
			yCoordinate = y;
			actionPoint --;
		}
	}
	
	//문을 회수하면서 갖고 있는 문에 하나를 추가한다.
	public void addDoor(int x, int y, String doorShape)
	{
		//문을 회수할 때는 main에 있는 전체 doors관리 array에서 해당 문이 삭제되어야 한다.
		if(actionPoint > 0)
		{
			hasDoors.add(new Door(x, y, doorShape));
		}
	}
	
	//밑으로 속성 리턴받는 함수들과 테스트 프린트 함수
	public int getActionPoint()
	{
		return actionPoint;
	}
	
		public void testPrint()
	{
		System.out.printf("이 캐릭터는 (%d, %d)에 위치하고 있으며, %d개의 캔디를 갖고 있습니다.\n", xCoordinate, yCoordinate, candy);
		for(Door door : hasDoors)
		{
			System.out.printf("갖고 있는 door의 스트링은 : %s\n", door.getShape());
		}
	}
	
}


public class Main
{
	public static void main(String args[])
	{
		//초기에 문 8개, NPC2개, PC2개 생성해야 함. 문은 계속 추가와 삭제가 가능토록 해야 함.
		//각 리스트 선언
		ArrayList<Door> doors = new ArrayList<Door>(); //맵에 깔린 Door를 관리하는 리스트
		ArrayList<PlayerCharacter> playerCharacters = new ArrayList<PlayerCharacter>();
		ArrayList<NounPlayerCharacter> nounPlayerCharacters = new ArrayList<NounPlayerCharacter>();
		
		//리스트에 초기 객체들 만들어 집어넣기
		//플레이어 캐릭터
		//근데 생각해보면 캐릭터별로 그래픽... 생김새가 달라야 하는데 안드스튜에서 같은 클래스로 관리할 수 있나? 일단은 리스트로 관리해보고 생각 지금은 모르니까
		playerCharacters.add(new PlayerCharacter(3, 0, "String"));
		playerCharacters.add(new PlayerCharacter(3, 4, "String"));
		
		nounPlayerCharacters.add(new NounPlayerCharacter(3, 2));
		nounPlayerCharacters.add(new NounPlayerCharacter(3, 3));
		
		doors.add(new Door(0, 0, "String"));
		doors.add(new Door(6, 0, "String"));
		doors.add(new Door(0, 1, "String"));
		doors.add(new Door(6, 1, "String"));
		doors.add(new Door(0, 2, "String"));
		doors.add(new Door(6, 2, "String"));
		doors.add(new Door(0, 3, "String"));
		doors.add(new Door(6, 3, "String"));
		//윗줄까지 게임 초기 세팅 !! 말과 문들을 올려두어요~
		
		System.out.printf("잘 들어갔나 뱅글뱅글 돌면서 확인, 문부터\n");
		for(Door door : doors)
		{
			door.testPrint();
		}
		for(NounPlayerCharacter npc : nounPlayerCharacters)
		{
			npc.testPrint();
		}
		for(PlayerCharacter pc : playerCharacters)
		{
			pc.testPrint();
		}
	}
}





/*public class Main
{
    public static void main(String args[])
		{

      System.out.printf("Hello, World");
    }
}*/
