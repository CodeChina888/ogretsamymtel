package game.worldsrv.fightParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class GuildInstParam implements ISerilizable {
    public int stage; // 关卡
    public List<Integer> hpMax = new ArrayList<>();	// 怪物最大血量
    public List<Integer> hpCur = new ArrayList<>();	// 怪物当前血量

    public GuildInstParam() {

    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(stage);
        out.write(hpMax);
        out.write(hpCur);
    }

    @Override
    public void readFrom(InputStream in) throws IOException {
        stage = in.read();
        hpMax = in.read();
        hpCur = in.read();
    }
}
