package game.worldsrv.guild;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GuildSkillData implements ISerilizable {
    public int id;	  // GuildSkill表0级sn
    public int level; // 技能等级

    public GuildSkillData(JSONObject jo) {
        this.id = jo.getIntValue("id");
        this.level = jo.getIntValue("level");
    }

    public GuildSkillData(int id, int level) {
        this.id = id;
        this.level = level;
    }

    public GuildSkillData() {
    }

    public int getSn() {
        return calcSn(id, level);
    }

    static public int calcSn(int id, int level) {
        return id+level;
    }
    static public int getIdBySn(int sn) {
        return sn/1000 * 1000;
    }

    static public int getLevelBySn(int sn) {
        return sn%1000;
    }

    /**
     * 把Json转换为List
     * @param json
     * @return
     */
    public static List<GuildSkillData> jsonToList(String json) {

        List<GuildSkillData> result = new ArrayList<GuildSkillData>();
        if(StringUtils.isBlank(json)){
            return result;
        }
        JSONArray ja = JSON.parseArray(json);
        for (int i = 0; i < ja.size(); i++) {
            GuildSkillData vo = new GuildSkillData(ja.getJSONObject(i));
            result.add(vo);
        }

        return result;
    }

    /**
     * 将List转换为Json
     * @param list
     * @return
     */
    public static String listToJson(List<GuildSkillData> list){
        JSONArray ja = new JSONArray();
        if (list != null) {
            for (GuildSkillData vo : list) {
                JSONObject jo = new JSONObject();
                jo.put("id", vo.id);
                jo.put("level", vo.level);
                ja.add(jo);
            }
        }
        return ja.toJSONString();
    }

    /**
     * 获取充值信息
     * @param list
     * @return
     */
    public static GuildSkillData getSkillData(List<GuildSkillData> list, int id){
        for(GuildSkillData chargeInfo : list){
            if(chargeInfo.id == id){
                return chargeInfo;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("level", level)
                .toString();
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(id);
        out.write(level);
    }

    @Override
    public void readFrom(InputStream in) throws IOException {
        id = in.read();
        level = in.read();
    }
}
