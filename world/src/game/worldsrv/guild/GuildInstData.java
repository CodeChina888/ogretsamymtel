package game.worldsrv.guild;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import game.msg.Define;
import org.apache.commons.lang3.builder.ToStringBuilder;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GuildInstData implements ISerilizable {
    public int chapter;	  // 章节
    public List<List<Integer>> hpMax = new ArrayList<>(); //怪物最大血量
    public List<List<Integer>> hpCur = new ArrayList<>(); //怪物当前血量，0即boss死亡
    public long lastResetTime; // 上次重置时间
    public Define.EGuildInstResetType resetType; // 1: 重置当前章节；2：重置回前一章节
    private transient HashMap<Integer, String> killerCache = new HashMap<>();

    public GuildInstData() {
    }

    public GuildInstData(String jsonStr) {
        JSONObject json = (JSONObject) JSON.parse(jsonStr);
        chapter = json.getIntValue("chapter");
        lastResetTime = json.getLongValue("lastResetTime");
        resetType = Define.EGuildInstResetType.valueOf(json.getString("resetType"));
        hpMax = parseLL(json, "hpMax");
        hpCur = parseLL(json, "hpCur");
    }

    public String toJsonStr() {
        JSONObject jo = new JSONObject();
        jo.put("chapter", chapter);
        jo.put("lastResetTime", lastResetTime);
        jo.put("resetType", resetType);
        jo.put("hpMax", LL2Ja(hpMax));
        jo.put("hpCur", LL2Ja(hpCur));
        return jo.toJSONString();
    }

    private List<List<Integer>> parseLL(JSONObject json, String key) {
        List<List<Integer>> LL = new ArrayList<>();
        JSONArray ja = json.getJSONArray(key);
        for (int i=0; i<ja.size(); ++i) {
            List<Integer> L = new ArrayList<>();
            JSONArray jaSub = ja.getJSONArray(i);
            for (Object hp : jaSub) {
                L.add((Integer)hp);
            }
            LL.add(L);
        }
        return LL;
    }

    private JSONArray LL2Ja(List<List<Integer>> LL) {
        JSONArray ja = new JSONArray();
        for (List<Integer> L : LL) {
            JSONArray jaSub = new JSONArray();
            for (Integer i : L) {
                jaSub.add(i);
            }
            ja.add(jaSub);
        }
        return ja;
    }

    static public int chapterSn(int order) {
        return 100+order;
    }

    static public int stageSn(int chapterOrder, int stageOrder) {
        return 401000+chapterOrder*10+stageOrder;
    }

    static public int chapterSnFromStageSn(int stageSn) {
        return chapterSn((stageSn%1000)/10);
    }

    static public int stageOrder(int stageSn) {
        return stageSn%10;
    }

    public int chapterOrder() {
        return chapter%100;
    }

    public boolean isStageOver(int idx) {
        if (idx < 0 || idx >= hpCur.size()) {
            return false;
        }
        List<Integer> hpL = hpCur.get(idx);
        if (hpL == null) {
            return false;
        }
        return hpL.isEmpty();
    }

    public boolean isChapterOver() {
        if (hpCur.size() == 0) {
            return false;
        }
        for (List<Integer> hpL : hpCur)  {
            if (!hpL.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public String getKiller(int stage) {
        return killerCache.get(stage);
    }

    public void setKiller(int stage, String killer) {
        if (!killerCache.containsKey(stage)) {
            killerCache.put(stage, killer);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("chapter", chapter)
                .append("maxHps", hpMax)
                .append("hpCur", hpCur)
                .append("lastResetTime", lastResetTime)
                .append("resetType", resetType)
                .toString();
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(chapter);
        out.write(hpMax);
        out.write(hpCur);
        out.write(lastResetTime);
        out.write(resetType);
    }

    @Override
    public void readFrom(InputStream in) throws IOException {
        chapter = in.read();
        hpMax = in.read();
        hpCur = in.read();
        lastResetTime = in.read();
        resetType = in.read();
    }
}
