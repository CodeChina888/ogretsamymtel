package game.worldsrv.guild;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.alibaba.fastjson.JSON;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class GuildInstStageRewardData implements ISerilizable {
    public HashMap<Integer, ArrayList<GuildInstStageRewardInfo>> stageMap = new HashMap<>();

    public GuildInstStageRewardData() {
    }

    static public GuildInstStageRewardData create(String jsonStr) {
        return JSON.parseObject(jsonStr, GuildInstStageRewardData.class);
    }

    public String toJsonStr() {
        return JSON.toJSONString(this);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(stageMap);
    }

    @Override
    public void readFrom(InputStream in) throws IOException {
        stageMap = in.read();
    }
}
