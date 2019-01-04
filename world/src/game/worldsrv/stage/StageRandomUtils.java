package game.worldsrv.stage;

import java.util.ArrayList;
import java.util.List;

import core.support.RandomUtils;

public class StageRandomUtils {
	public List<Integer> seek = new ArrayList<Integer>();
	public int seekCount = 0;
	public int seekCur = 0;

	public float div = 10000f;

	public StageRandomUtils(List<Integer> seek) {
		this.seek = seek;
		this.seekCount = seek.size();
		this.seekCur = 0;
	}

	public StageRandomUtils(int count) {
		this.seekCur = 0;
		this.seekCount = count;
		for (int i = 0; i < seekCount; i++) {
			seek.add(RandomUtils.nextInt((int) div));
		}
	}

	public double nextDouble() {
		return seek.get(index()) / div;
	}

	public int nextInt(int range) {
		double result = seek.get(index()) / div;
		return (int) (Math.floor(result * range));
	}

	public int index() {
		seekCur++;
		if (seekCur >= seekCount) {
			seekCur = 0;
		}
		return seekCur;
	}
}
