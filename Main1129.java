//기존 코드 맨 밑으로 내려두고 기말 프로젝트 코드 작성 중
//더 해야 하는 것 : 


import java.util.*;

//주사위 굴리는 용도의 클래스
class Dice
{
	private static Random rollingNumber = new Random(); //주사위 객체를 만들어두어요
	
	//a면체 주사위를 굴려요 1~a
	public static int rollingDice(int a)
	{
		return rollingNumber.nextInt(a) + 1;
	}
	
	//문을 랜덤으로 뽑을 때 사용하려고 만든 함수이나
	//랜덤으로 뽑으면 문 갯수 조절과 위치 배정에 어려움이 있어 쓰지 않음
	/*public static String rollingDoor()
	{
		int dice = rollingNumber.nextInt(4); //1~4
		switch(dice)
		{
			case 1:
				return "C";
			case 2:
				return "W";
			case 3:
				return "N";
			case 4:
				return "D";
			default:
				return "error";
		}
	}*/
}


class Door
{
	//door는? 생성되거나 제거될 수 있다. (좌표값)이동은 불가하다. 사용시 다른 문과 연결.
	//그럼 x,y값은 final로 주어도 무방하다
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
		System.out.printf("이 door은 (%d, %d)에 위치한 %s입니다.\n", xCoordinate, yCoordinate, shape);
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
		System.out.printf("이 캐릭터는 (%d, %d)에 위치하고 있으며, %d개의 캔디를 갖고 있습니다.\n", xCoordinate, yCoordinate, candy);
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
	
	public void rollingDice()
	{
		actionPoint = Dice.rollingDice(6); //액션 포인트 추가가 아니라 고정값으로 +=이 아니다.
	}
	
	public void moveRight()
	{
		if(xCoordinate != 6) //오른쪽 맨 끝 칸이 아닌 경우
		{
			xCoordinate ++;
			actionPoint --;
		}
		else
		{
			System.out.println("해당 방향으로 이동할 수 없습니다.");
		}
	}
	
	public void moveLeft()
	{
		if(xCoordinate != 0)
		{ //액션포인트는 main에서 체크하도록 하여서(액션포인트가 잔존한 한 턴을 이어간다) 좌표만 체크한다.
			xCoordinate --;
			actionPoint --;
		}
		else
		{
			System.out.println("해당 방향으로 이동할 수 없습니다.");
		}
	}
	
	public void useDoor(int x, int y) //문을 사용할 때 x,y를 받아서 한번에 이동한다.
	{
		//문을 사용할 때는 나왔던 문의 isitused 값이 변경되어야 한다.
		xCoordinate = x;
		yCoordinate = y;
		actionPoint --;
	}
	
	//문을 회수하면서 갖고 있는 문에 하나를 추가한다.
	public void addDoor(int x, int y, String doorShape)
	{
		//문을 회수할 때는 main에 있는 전체 doors관리 array에서 해당 문이 삭제되어야 한다.
		hasDoors.add(new Door(x, y, doorShape));
		actionPoint--;
	}
	
	//index값에 해당하는 위치의 Door를 지운다
	public void deleteDoor(int index)
	{
		hasDoors.remove(index);
		actionPoint --;
	}
	
	public void secondRolled()
	{
		secondRoll = true;
	}
	
	public void resetSecondRoll()
	{
		secondRoll = false;
	}

	
	//소지하고 있는 Doors리스트를 반환하는 함수인데 이런게 되나? >됨.
	public ArrayList<Door> getHasDoors()
	{
		return hasDoors;
	}
	
	//밑으로 속성 리턴받는 함수들과 테스트 프린트 함수
	public int getActionPoint()
	{
		return actionPoint;
	}
	public boolean getSecondRoll()
	{
		return secondRoll;
	}
	
		public void testPrint()
	{
		System.out.printf("이 캐릭터는 (%d, %d)에 위치하고 있으며, %d개의 캔디를 갖고 있습니다. 남은 AP는 %d입니다.\n", xCoordinate, yCoordinate, candy, actionPoint);
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
		//스캐너랑 스캔 받을 input
		Scanner input = new Scanner(System.in);
		String action; //pc에게서 받은 액션.
		int selectDoor; //이동할 door의 인덱스를 지정할 때 사용합니다. string을 받아서 int로 변환해도 되긴 하는데 일단 int로도 갖고 있기로 해봄.
		//초기에 문 8개, NPC2개, PC2개 생성해야 함. 문은 계속 추가와 삭제가 가능토록 해야 함.
		//각 리스트 선언
		ArrayList<Door> doors = new ArrayList<Door>(); //맵에 깔린 Door를 관리하는 리스트
		ArrayList<PlayerCharacter> playerCharacters = new ArrayList<PlayerCharacter>();
		ArrayList<NounPlayerCharacter> nounPlayerCharacters = new ArrayList<NounPlayerCharacter>();
		
		//리스트에 초기 객체들 만들어 집어넣기
		//플레이어 캐릭터
		//근데 생각해보면 캐릭터별로 그래픽... 생김새가 달라야 하는데 안드스튜에서 같은 클래스로 관리할 수 있나? 일단은 리스트로 관리해보고 생각 지금은 모르니까
		playerCharacters.add(new PlayerCharacter(3, 0, "N"));
		playerCharacters.add(new PlayerCharacter(3, 3, "D"));
		
		nounPlayerCharacters.add(new NounPlayerCharacter(3, 2));
		nounPlayerCharacters.add(new NounPlayerCharacter(3, 3));
		
		doors.add(new Door(0, 0, "N"));
		doors.add(new Door(6, 0, "W"));
		doors.add(new Door(0, 1, "D"));
		doors.add(new Door(6, 1, "C"));
		doors.add(new Door(0, 2, "W"));
		doors.add(new Door(6, 2, "C"));
		doors.add(new Door(0, 3, "N"));
		doors.add(new Door(6, 3, "D"));
		//윗줄까지 게임 초기 세팅, 초기 캐릭터와 문들을 리스트에 올린다.
		
		/* //세팅이 잘 들어갔는지 출력하는 부분
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
		*/
		
		
		//게임 진행, 10라운드간
		for(int round = 0; round < 10; round++)
		{
			//pc돌면서 행동 선언
			for(PlayerCharacter pc : playerCharacters)
			{
				System.out.println("첫 주사위를 굴려 액션 포인트를 획득하세요. 주사위 굴리기 명령어는 D");
				action = input.nextLine();
				if(action.equals("D"))
				{
					pc.rollingDice();
					System.out.printf("주사위를 굴려 얻은 액션 포인트는 %d입니다.\n", pc.getActionPoint());
				}
				
				//액션포인트가 잔존하는 한 계속 액션
				hasActionWhile:
				while(pc.getActionPoint() > 0)
				{
					System.out.println("행동 선언. 오른쪽 이동은 R, 왼쪽 이동은 L, 문 사용은 D, 문 회수는 A, 문 생성은 M");
					action = input.nextLine();
					
					//"D"를 입력해 자신 위치의 문을 사용하는 경우
					if(action.equals("D"))
					{
						//현재 위치한 곳에 있는 문의 정보를 받아서, (고민 지점 : 리스트에 있는 shape를 모두 확인해야 할텐데,...
						//for문으로 doors를 쭉 도는 것 밖에 방법이 없을까?)
						//그 문과 같은 모양의 문들의 정보를 받아서,
						//그 문으로 이동이 가능하게끔 함.
						//나온 문은 사용되었음을 확인하는 변수가 변경 됨.
						for(Door door : doors)
						{
							//모든 문을 돌면서 pc랑 같은 위치에 있는 문이 있는지 확인함. 이건 항상 0~1개
							if(door.getX() == pc.getX() && door.getY() == pc.getY())
							{
								//pc의 위치에 문이 있다면
								//shape값이 같은 이동 가능한 문들을 저장할 배열 생성
								ArrayList<Integer> indexList = new ArrayList<Integer>();
								System.out.printf("사용할 수 있는 문이 있습니다. 해당 문의 모양은 %s입니다.\n", door.getShape());
								
								//이 문에서 shape를 받아서 다시 모든 문을 돌면서 같은 모양의 문이 있는지 확인합니다
								//또한 indexList에 인덱스들을 추가해서 차후 플레이어가 인덱스를 선택해서 이동할 수 있도록 합니다
								for(Door compDoor : doors)
								{
									//모양이 같으면서 인덱스가 동일하지 않은 경우 추가합니다
									if(door.getShape().equals(compDoor.getShape()) &&
										 doors.indexOf(door) != doors.indexOf(compDoor))
									{
										//사용한 문, 나올 수 있는 문이 전부 나오는 것을 확인했습니다.
										System.out.printf("사용할 수 있는 문과 모양이 같은 문의 인덱스는 %d입니다. \n",
																			doors.indexOf(compDoor));
										indexList.add(doors.indexOf(compDoor));
									}
								}//나올 문 체크하는 for문을 닫습니다.
								System.out.printf("이동할 문을 선택해 주십시오.\n");
								selectDoor = input.nextInt(); //이동할 문 선택
								input.nextLine();
								//System.out.printf("방금 전에 입력한 이동할 문 : %d\n", selectDoor);
								if(indexList.contains(selectDoor)) //이동할 문이 이동 가능한 문 리스트에 있다면!!
								{
									//PC를 이동시키고
									pc.useDoor(doors.get(selectDoor).getX(), doors.get(selectDoor).getY());
									//나온 door에 이동되었음을 확인하는 변수 수정
									doors.get(selectDoor).useIt();
									//System.out.printf("ust It 잘 되는지 확인 : %d\n", doors.get(selectDoor).getIsItUsed());
									break;
								}
							}//pc가 사용할 수 있는 문이 있는 경우의 if문을 닫습니다
						}//pc가 사용할 수 있는 문을 체크하는 for문을 닫습니다
						
					}//"D"를 입력해 D를 사용하는 경우의 if문을 닫습니다
					
					//문 회수
					else if(action.equals("A"))
					{
						for(Door door : doors)
						{
							if(pc.getX() == door.getX() && pc.getY() == door.getY() && door.getIsItUsed())
							{
								pc.addDoor(door.getX(), door.getY(), door.getShape());
								door.resetUse();
								doors.remove(door);
								break;
							}
						}//doors를 돌면서 현재 위치에 Door이 있는지 확인하는 for문 종료 
					}//"A"를 입력해 문을 회수하는 경우의 if문을 닫습니다
					
					//문 생성
					else if(action.equals("M"))
					{
						//같은 위치에 다른 문이 있는지 확인하는 for문
						for(Door door : doors)
						{
							if(pc.getX() == door.getX() && pc.getY() == door.getY())
							{
								System.out.println("이곳에는 이미 다른 문이 존재합니다.");
								continue hasActionWhile;
							}
						}
						//갖고 있는 문 보여주자
						pc.testPrint();
						System.out.println("설치할 문의 index(0부터 시작)을 입력해주십시오.");
						
						//설치할 문의 index번호를 받아서
						selectDoor = input.nextInt();
						input.nextLine();
						
						//필드에 설치
						doors.add(pc.getHasDoors().get(selectDoor));
						//pc의 hasDoors에서 삭제
						pc.deleteDoor(selectDoor);
					}//"M"을 입력해 필드에 문을 생성하는 경우의 if문을 닫습니다.
					
					//오른쪽 이동
					else if(action.equals("R"))
					{
						pc.moveRight();
						//이동 후 캔디 먹을 건덕지 확인, npc리스트부터 돈다
						for(NounPlayerCharacter npc : nounPlayerCharacters)
						{
							if(pc.getX() == npc.getX() && pc.getY() == npc.getY() &&
								 npc.getCandy() > 0 && !npc.getIsItAttacked())
							{
								npc.attacked();
								pc.earnCandy();
								//캔디 먹은 뒤에는 주사위를 다시 굴려야 함(본인 턴에 1회만 가능)
								if(!pc.getSecondRoll()) //아직 second Roll하지 않은 경우
								{
									System.out.println("두번째 주사위를 굴려 액션 포인트를 획득하세요. 주사위 굴리기 명령어는 D");
									action = input.nextLine();
									if(action.equals("finish"))
									{
										return;
									}
									if(action.equals("D"))
									{
										pc.rollingDice();
										pc.secondRolled();
										System.out.printf("주사위를 굴려 얻은 액션 포인트는 %d입니다.\n", pc.getActionPoint());
									}
									break;
								}
							}//같은 칸에 있는 npc리스트 확인하는 if문 끝
						}//캔디 먹는 거 확인용으로 npc리스트 도는 거 끝
						for(PlayerCharacter pc2 : playerCharacters)
						{//리스트에서 자신이 아닌 다른 pc캐릭터이며 위치가 같은 경우
							if(pc != pc2 && pc.getX() == pc2.getX() && pc.getY() == pc2.getY()
								 && pc2.getCandy() > 0 && !pc2.getIsItAttacked())
							{
								pc2.attacked();
								pc.earnCandy();
								if(!pc.getSecondRoll())
								{
									System.out.println("두번째 주사위를 굴려 액션 포인트를 획득하세요. 주사위 굴리기 명령어는 D");
									action = input.nextLine();
									if(action.equals("finish"))
									{
										return;
									}
									if(action.equals("D"))
									{
										pc.rollingDice();
										pc.secondRolled();
										System.out.printf("주사위를 굴려 얻은 액션 포인트는 %d입니다.\n", pc.getActionPoint());
									}
									break;
								}
							}
						}//캔디 먹는거 확인용으로 pc2리스트 도는 거 끝
					}//"R"을 입력해 오른쪽 이동하는 경우의 if문을 닫습니다
					
					else if(action.equals("L"))
					{
						pc.moveLeft();
						//이동 후 캔디 먹을 건덕지 확인, npc리스트부터 돈다
						for(NounPlayerCharacter npc : nounPlayerCharacters)
						{
							if(pc.getX() == npc.getX() && pc.getY() == npc.getY() &&
								npc.getCandy() > 0 && !npc.getIsItAttacked())
							{
								npc.attacked();
								pc.earnCandy();
								if(!pc.getSecondRoll())
								{
									System.out.println("두번째 주사위를 굴려 액션 포인트를 획득하세요. 주사위 굴리기 명령어는 D");
									action = input.nextLine();
									if(action.equals("finish"))
									{
										return;
									}
									if(action.equals("D"))
									{
										pc.rollingDice();
										pc.secondRolled();
										System.out.printf("주사위를 굴려 얻은 액션 포인트는 %d입니다.\n", pc.getActionPoint());
									}
									break;
								}
							}
						}//캔디 먹는 거 확인용으로 npc리스트 도는 거 끝
						for(PlayerCharacter pc2 : playerCharacters)
						{//리스트에서 자신이 아닌 다른 pc캐릭터이며 위치가 같은 경우
							if(pc != pc2 && pc.getX() == pc2.getX() && pc.getY() == pc2.getY() &&
								 pc2.getCandy() > 0 && !pc2.getIsItAttacked())
							{
								pc2.attacked();
								pc.earnCandy();
								if(!pc.getSecondRoll())
								{
									System.out.println("두번째 주사위를 굴려 액션 포인트를 획득하세요. 주사위 굴리기 명령어는 D");
									action = input.nextLine();
									if(action.equals("finish"))
									{
										return;
									}
									if(action.equals("D"))
									{
										pc.rollingDice();
										pc.secondRolled();
										System.out.printf("주사위를 굴려 얻은 액션 포인트는 %d입니다.\n", pc.getActionPoint());
									}
									break;
								}
							}
						}//캔디 먹는거 확인용으로 pc2리스트 도는 거 끝
					}//"L"을 입력해 왼쪽 이동하는 경우의 if문을 닫습니다
					
					//명령어가 아닌 다른 걸 입력한 경우
					else
					{
						System.out.printf("명령어를 다시 입력해주십시오.\n");
						continue;
					}
					
					System.out.printf("한 행동을 마쳤습니다.\n");
					pc.testPrint();
				}//한 pc의 액션포인트가 잔존하는 동안 계속 턴 갖는 거 끝
				pc.resetSecondRoll();
				System.out.printf("턴을 마쳤습니다.\n");
				//pc턴 종료마다 모든 문 사용했는지 확인 하는 것 0으로 초기화	
				for(Door door : doors)
				{
					door.resetUse();
				}
				//pc턴 종료마다 모든 캐릭터 공격당했는지 확인 하는 것 0으로 초기화
				for(NounPlayerCharacter npc : nounPlayerCharacters)
				{
					npc.resetAttacked();
				}
				for(PlayerCharacter pc2 : playerCharacters)
				{
					pc.resetAttacked();
				}
				
			}//pc두명 도는 for문 끝
			
			//두명의 pc행동을 마친 뒤 두 npc도 이동시키고 다음 라운드
			for(NounPlayerCharacter npc : nounPlayerCharacters)
			{
				//이동할 때 이미 pc가 있는 자리로 이동한다면 어떻게 처리할 지 고민의 여지가 있 음...
				//다른 npc랑은 영원히 마주칠 일 없으니 pc와의 충돌만 고려하면 됨
				npc.randomMove();
				for(PlayerCharacter pc : playerCharacters)
				{
					if(npc.getX() == pc.getX() && npc.getY() == pc.getY())
					{
						pc.attacked();
						npc.earnCandy();
					}
				}//pc두명 돌면서 npc와의 충돌이 있는지 확인하는 for문 끝
				
				//npc의 한 턴이 끝나면 isItAttacked를 초기화해요
				for(PlayerCharacter pc : playerCharacters)
				{
					pc.resetAttacked();
				}
				
				npc.testPrint();
			}//npc두명 돌면서 랜덤 이동시키는 for문 끝
			
		}//10라운드 플레이 for문 끝
		if(playerCharacters.get(0).getCandy() > playerCharacters.get(1).getCandy())
		{
			System.out.printf("Player0이 %d개의 사탕, Player1이 %d개의 사탕을 소지하여 Player0의 승리입니다.\n",
											 playerCharacters.get(0).getCandy(), playerCharacters.get(1).getCandy());
		}
		else if(playerCharacters.get(0).getCandy() < playerCharacters.get(1).getCandy())
		{
			System.out.printf("Player0이 %d개의 사탕, Player1이 %d개의 사탕을 소지하여 Player1의 승리입니다.\n",
											 playerCharacters.get(0).getCandy(), playerCharacters.get(1).getCandy());
		}
		else
		{
			System.out.printf("Player0이 %d개의 사탕, Player1이 %d개의 사탕을 소지하여 무승부입니다.\n",
											 playerCharacters.get(0).getCandy(), playerCharacters.get(1).getCandy());
		}
		
	}//public static main 끝
}//class Main 끝





/*public class Main
{
    public static void main(String args[])
		{

      System.out.printf("Hello, World");
    }
}*/
