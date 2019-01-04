package game.platform.login;


public enum LoginType{
	PC(1),MI(2),FACEBOOK(3),IOS(4),SINA(5),LANMAO(6),QuickSDK(7),YYBSQ(8),YYBWX(9),QuickNormal(10);
	public int value;
	
	LoginType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	

    public static LoginType valueOf(int value) {
        switch (value) {
        case 1:
            return LoginType.PC;
        case 2:
            return LoginType.MI;
        case 3:
            return LoginType.FACEBOOK;
        case 4:
            return LoginType.IOS;
        case 5:
            return LoginType.SINA;
        case 6:
            return LoginType.LANMAO;
        case 7:
            return LoginType.QuickSDK;   
        case 8:
        	return LoginType.YYBSQ;
        case 9:
        	return LoginType.YYBWX;
        case 10:
        	return LoginType.QuickNormal;
        default:
            return null;
        }
    }
	
}
