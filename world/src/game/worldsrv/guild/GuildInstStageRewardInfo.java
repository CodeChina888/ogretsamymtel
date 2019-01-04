package game.worldsrv.guild;

import java.io.IOException;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.builder.ToStringBuilder;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import com.alibaba.fastjson.JSONObject;

public class GuildInstStageRewardInfo implements ISerilizable {
    public int slot; // 格子索引0开始
    public String playerName; // 玩家名字
    public int itemSn; // 物品Sn
    public int itemNum; // 物品数量

    public GuildInstStageRewardInfo() {
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("slot", slot)
                .append("playerName", playerName)
                .append("itemSn", itemSn)
                .append("itemNum", itemNum)
                .toString();
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(slot);
        out.write(playerName);
        out.write(itemSn);
        out.write(itemNum);
    }

    @Override
    public void readFrom(InputStream in) throws IOException {
        slot = in.read();
        playerName = in.read();
        itemSn = in.read();
        itemNum = in.read();
    }
}
