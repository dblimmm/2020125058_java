import java.util.*;

//주사위 굴리는 클래스
class Dice
{
	private static Random rollingNumber = new Random();
	
	//a면체 주사위를 굴려요
	public static int RollingDice(int a)
	{
		return rollingNumber.nextInt(a) + 1;
	}
}


class Door
{
	//door는? 생성되거나 제거될 수 있다. (좌표값)이동은 불가하다. 사용시 다른 문과 연결.
	//그럼 x,y값은 final로 줘도 되겠네
	final private int xCoordinate;
	final private int yCoordinate;
	final private String shape;
	
	//생성자
	//무슨 종류의 문인지?(4종) 추가해야겠네
	public Door(int x, int y, String shape)
	{
		xCoordinate = x;
		yCoordinate = y;
		this.shape = shape;
	}
	
	//x값과 y값을 리턴하는 함수(이걸 받아서 캐릭터가 그 위치로 이동해야 함)
	//수정해야 함 xy를 array로 주면 되나 ? ... getinfo로 하나씩 빼야 하나 무슨 문인지도 return하는게 필요한데!
	public int GetPosition()
	{
		return xCoordinate;
	}
}

//캐릭터에서 논플레이어캐릭터/플레이어캐릭터 상속받으면 될 듯
//Character이라는 기본 클래스가 있는 듯? 중간에 하나 대문자로 했는데 다른 방법 없나~
class ChaRacter
{
	//모든 캐릭터는 x. y좌표가 있고 이동이 가능 함. 캔디를 가지고 있음.
	//플레이어 캐릭터는 문을 사용할 수 있음.
	//플레이어 캐릭터는 주사위를 굴릴 수 있음. AP(액션 포인트)를 갖고 있어야함.
	//상속하니까 private이면 안되지! protected
	protected int xCoordinate;
	protected int yCoordinate;
	protected int candy; //소지 캔디 갯수
	//전체 캐릭터는 해당 라운드에 공격을 당했는지 안당했는지 표시하는 속성이 있어야 함
	
	//생성자
	public ChaRacter(int x, int y, int candy)
	{
		xCoordinate = x;
		yCoordinate = y;
		this.candy = candy;
	}

	public void MoveRight()
	{
		xCoordinate ++;
	}
	
	public void MoveLeft()
	{
		yCoordinate ++;
	}
}

class NounPlayerCharacter extends ChaRacter
{
	public NounPlayerCharacter(int x, int y, int candy)
	{
		super(x, y, candy);
	}
}

class PlayerCharacter extends ChaRacter
{
	private int actionPoint;
	//갖고 있는 문의 종류에 대한 리스트를 갖고 있어야 함 (근데 이걸 클래스의 속성으로 넣을지 메인 함수에서 관리가 될지)
	//private String doorShape;
	//플레이어 캐릭터는 행동 시 AP가 깎이는 것을 여차저차 만들어야 함
	
	public PlayerCharacter(int x, int y, int candy)
	{
		super(x, y, candy);
	}
	
	public void RollingDice()
	{
		actionPoint = Dice.RollingDice(6);
	}
	
	public void MoveDoor(int x, int y) //x, y를 받아서
	{
		xCoordinate = x;
		yCoordinate = y;
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
		ArrayList<NounPlayerCharacter> nounPlayerCharcters = new ArrayList<NounPlayerCharacter>();
		
		//리스트에 초기 객체들 만들어 집어넣기
		//플레이어 캐릭터
		//근데 생각해보면 캐릭터별로 그래픽... 생김새가 달라야 하는데 안드스튜에서 같은 클래스로 관리할 수 있나? 일단은 리스트로 관리해보고 생각 지금은 모르니까
		playerCharacters.add(new PlayerCharacter(1, 4, 3));
		playerCharacters.add(new PlayerCharacter(4, 4, 3));
		
		nounPlayerCharcters.add(new NounPlayerCharacter(2, 4, 3));
		nounPlayerCharcters.add(new NounPlayerCharacter(2, 4, 3));
		
		doors.add(new Door(1, 1, "String"));
		doors.add(new Door(1, 7, "String"));
		doors.add(new Door(2, 1, "String"));
		doors.add(new Door(2, 7, "String"));
		doors.add(new Door(3, 1, "String"));
		doors.add(new Door(3, 7, "String"));
		doors.add(new Door(4, 1, "String"));
		doors.add(new Door(4, 7, "String"));
		
		System.out.printf("");
	}
}
