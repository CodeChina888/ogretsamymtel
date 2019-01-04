package game.worldsrv.test;

public class ClassA {
	public static void main(String[] args) {
		C c = new C();
		System.out.println(c instanceof ClassA);
	}
}
class C extends B{
	
}
class B extends ClassA{
	
}
