package game.worldsrv.partner;
/**
 * 类型
 * @author songy
 *
 */
public enum ObjectType {
	Human("主角", 1), 
	Partner("伙伴", 2), 
	Cimelia("法宝", 3);
    
    private String name ;
    private int index ;
     
    private ObjectType( String name , int index ){
        this.name = name ;
        this.index = index ;
    }
     
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}
