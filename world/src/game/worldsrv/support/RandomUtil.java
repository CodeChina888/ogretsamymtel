package game.worldsrv.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 随机数工具
 * @author Aivs.Gao
 */
public class RandomUtil {

	/**
	 * Logger for this class
	 */
	private static Random random = new Random();

	public static int random(int max) {
		return random.nextInt(max);
	}

	/**
	 * 包含最大最小值
	 *
	 * @param min
	 * @param max
	 * @return
	 */
	public static int random(int min, int max) {
		int minNum = min < max ? min : max;
		int maxNum = min < max ? max : min;
		return minNum + random.nextInt(maxNum - minNum + 1);
	}

	/**
	 * 根据几率 计算是否生成
	 *
	 * @param probability
	 * @return
	 */
	public static boolean isGenerate(int probability, int gailv) {
		if (gailv == 0) {
			gailv = 1000;
		}
		int random_seed = random.nextInt(gailv + 1);
		return probability >= random_seed;
	}

	/**
	 *
	 * gailv/probability 比率形式
	 *
	 * @param probability
	 * @param gailv
	 * @return
	 */
	public static boolean isGenerate2(int probability, int gailv) {
		if (probability == gailv) {
			return true;
		}
		if (gailv == 0) {
			return false;
		}
		int random_seed = random.nextInt(probability);
		return random_seed + 1 <= gailv;
	}

	/**
	 * 根据几率 计算是否生成
	 *
	 * @param probability
	 * @return
	 */
	public static boolean defaultIsGenerate(int probability, int baseProb) {
		int random_seed = random.nextInt(baseProb);
		return probability >= random_seed;
	}

	/**
	 * 从 min 和 max 中间随机一个值
	 *
	 * @param max
	 * @param min
	 * @return 包含min max
	 */
	public static int randomValue(int max, int min) {
		int temp = max - min;
		temp = RandomUtil.random.nextInt(temp + 1);
		temp = temp + min;
		return temp;
	}

	/**
	 * 返回在0-maxcout之间产生的随机数时候小于num
	 *
	 * @param num
	 * @return
	 */
	public static boolean isGenerateToBoolean(float num, int maxcout) {
		double count = Math.random() * maxcout;

		if (count < num) {
			return true;
		}
		return false;
	}

	/**
	 * 返回在0-maxcout之间产生的随机数时候小于num
	 *
	 * @param num
	 * @return
	 */
	public static boolean isGenerateToBoolean(int num, int maxcout) {
		double count = Math.random() * maxcout;

		// System.out.println("计算========"+ count);
		// System.out.println("传入========"+ num);
		// System.out.println("计算<传入");
		// System.out.println(count<num);
		if (count < num) {
			return true;
		}
		return false;
	}

	/**
	 * 随机产生min到max之间的整数值 包括min max
	 *
	 * @param min
	 * @param max
	 * @return
	 */
	public static int randomIntValue(int min, int max) {
		return (int) (Math.random() * (double) (max - min + 1)) + min;
	}

	public static float randomFloatValue(float min, float max) {
		return (float) (Math.random() * (double) (max - min)) + min;
	}

	public static <T> T randomItem(Collection<T> collection) {
		if (collection == null || collection.size() == 0) {
			return null;
		}
		int t = (int) (collection.size() * Math.random());
		int i = 0;
		for (Iterator<T> item = collection.iterator(); i <= t && item.hasNext();) {
			T next = item.next();
			if (i == t) {
				return next;
			}
			i++;
		}
		return null;
	}

	/**
	 *
	 *            根据总机率返回序号
	 * @return
	 */
	public static int randomIndexByProb(List<Integer> probs) {
		try {
			LinkedList<Integer> newprobs = new LinkedList<Integer>();
			for (int i = 0; i < probs.size(); i++) {
				// if (probs.get(i) > 0) {
				if (i == 0) {
					newprobs.add(probs.get(i));
				} else {
					newprobs.add(newprobs.get(i - 1) + probs.get(i));
				}
				// }
			}
			if (newprobs.size() <= 0) {
				return -1;
			}
			int last = newprobs.getLast();
			if (last == 0) {
				return -1;
			}
			// String[] split = last.split(Symbol.XIAHUAXIAN_REG);
			int random = random(last);
			for (int i = 0; i < newprobs.size(); i++) {
				int value = newprobs.get(i);
				// String[] split2 = string.split(Symbol.XIAHUAXIAN_REG);
				// if(Integer.parseInt(split2[1])>random){
				if (value > random) {
					return i;
				}
			}
		} catch (Exception e) {
			Log.random.error("计算机率错误" + probs.toString(), e);
		}
		return -1;
	}

	/**
	 *
	 *            根据总机率返回序号
	 * @return
	 */
	public static int randomIndexByProb(Integer[] probs) {
		List<Integer> list = Arrays.asList(probs);
		return randomIndexByProb(list);
	}

	/**
	 * 非均匀分布的数组，返回命中数组元素的索引 全未命中返回-1
	 *
	 * @param rateArray
	 *            数组中各元素的值为该元素被命中的权重
	 * @return 命中的数组元素的索引
	 */
	public static int random(Integer[] rateArray) {
		int[] rateArrayInt = new int[rateArray.length];
		for (int i = 0; i < rateArray.length; i++) {
			rateArrayInt[i] = rateArray[i];
		}
		return random(rateArrayInt);
	}

	/**
	 * 非均匀分布的数组，返回命中数组元素的索引 全未命中返回-1
	 *
	 * @param rateArray
	 *            数组中各元素的值为该元素被命中的权重
	 * @return 命中的数组元素的索引
	 */
	public static int random(int[] rateArray) {
		if (null == rateArray) {
			throw new IllegalArgumentException("The random array must not be null!");
		}
		int arrayLength = rateArray.length;
		if (arrayLength == 0) {
			throw new IllegalArgumentException("The random array's length must not be zero!");
		}
		// 依次累加的和
		int rateSum = 0;
		// 从头开始 依次累加之后的各个元素和 的临时数组
		int[] rateSumArray = new int[arrayLength];

		for (int i = 0; i < arrayLength; i++) {

			if (rateArray[i] < 0) {
				throw new IllegalArgumentException("The array's element must not be equal or greater than zero!");
			}
			rateSum += rateArray[i];
			rateSumArray[i] = rateSum;
		}
		if (rateSum <= 0) {
			// 所有概率都为零，必然没有选中的元素，返回无效索引:-1
			return -1;
		}

		int randomInt = random(1, rateSum);
		int bingoIndex = -1;
		for (int i = 0; i < arrayLength; i++) {
			if (randomInt <= rateSumArray[i]) {
				bingoIndex = i;
				break;
			}
		}
		if (bingoIndex == -1) {
			throw new IllegalStateException("Cannot find out bingo index!");
		}
		return bingoIndex;
	}

	
	/**
	 * 根据权重列表得到不重复的随机索引
	 *
	 * @param weightList
	 *            权重数组
	 * @param count
	 *            需要的随机个数
	 * @return 当权重总和为0时返回空数组
	 */
	public static int[] getRandomUniqueIndex(List<Integer> weightList, int count) {
		if (weightList == null || count > weightList.size()) {
			return new int[0];
		}
		List<Integer> weights = new ArrayList<Integer>();
		for (Integer w : weightList) {
			weights.add(w);
		}
		int[] results = new int[count];
		List<Integer> tempResults = new ArrayList<Integer>();
		for (int i = 0; i < count; i++) {
			int index = random(weights.toArray(new Integer[0]));
			if (index == -1) {
				results = new int[0];
				break;
			}
			weights.remove(index);
			for (int j : tempResults) {
				if (j <= index) {
					index++;
				}
			}
			results[i] = index;
			tempResults.add(index);
			Collections.sort(tempResults);
		}
		return results;
	}
	
	/**
     * 取得指定范围内整數
     *
     * @param n 范围值, 取值[0,n)之间的随机数, n可取负数
     * @return
     */
    public static int nextInt(int n) {
        if (n == 0) {
            return 0;
        }
        int res = Math.abs(UUID.randomUUID().hashCode()) % n;
        return res;
    }

    // 取得整數+偏移
    /**
     * 取得指定范围内整數, 并加上 offset
     *
     * @param n
     * @param offset
     * @return
     */
    public static int nextInt(int n, int offset) {
        if (n == 0) {
            return offset;
        }
        int res = Math.abs(UUID.randomUUID().hashCode()) % n;
        return res + offset;
    }

    /**
     * 真假随机
     *
     * @return
     */
    public static boolean nextBoolean() {
        return (nextInt(2) == 1);
    }

    /**
     * 256范围内随机取值
     *
     * @return
     */
    public static byte nextByte() {
        return (byte) nextInt(256);
    }

    /**
     * 64位长整数随机
     *
     * @param n
     * @return
     */
    public static long nextLong(long n) {
        if (n == 0) {
            return 0;
        }
        long head = nextInt(Integer.MAX_VALUE);
        long l = nextInt(Integer.MAX_VALUE);

        long dividend = ((head << 32) + l);

        long remain = dividend - (dividend / n) * n;

        if (n < 0) {
            return 0 - remain;
        } else {
            return remain;
        }
    }
    
    /**
     * 按权重随机选取
     * @param weights, 权重列表
     * @return 
     */
    public static int roll(List<Integer> weights){
        if(weights.isEmpty())
            return -1;
        
        if(weights.size() == 1)
            return 0;
        
        int sum = 0;
        for (Integer w : weights) {
            sum += w;
        }
        
        int r = nextInt(sum);
        
        int tmpw = 0;
        int min,max;
        
        for (int i = 0; i < weights.size(); i++) {
            min = tmpw;
            tmpw += weights.get(i);
            max = tmpw;
            if(r > min && r <= max){
                return i;
            }
        }
        
        return 0;
    }
    
    /**
     * 产生一个处于[0,99]之间的随机整数
     *
     * @return
     */
    public static int getRandomInt() {
        return Math.abs(nextInt(100));
    }
    
    /**
     * 产生一个处于[0,Delta]之间的随机整数
     *
     * @param iDelta
     * @return
     */
    public static int getRandomInt(int iDelta) {
        return Math.abs(nextInt(iDelta + 1));
    }
    
    /**
     * rateList 含有各项的权重， totalRate是总权重
     *
     * @param _rateList
     * @param _totalRate
     * @return 获取列表_rateList的下标编号
     */
    public static int getRandomRateByList(List<Integer> _rateList, int _totalRate) {
        if (0 == _totalRate) {
            return -1;
        }

        int rand = getRandomInt(_totalRate - 1) + 1;

        for (int index = 0; index < _rateList.size(); index++) {
            int curRate = _rateList.get(index);
            if (rand <= curRate) {
                return index;
            }
            rand -= curRate;
        }
        return -1;
    }
    
    /**
     * rateList 含有各项的权重
     *
     * @param _rateList
     * @return
     */
    public static int getRandomIndexByRate(List<Integer> _rateList) {
        int rand = 0;

        for (Integer rate : _rateList) {
            rand += rate;
        }

        return getRandomRateByList(_rateList, rand);
    }

    
    /**
     * rateList 含有各项的权重
     *
     * @param <T>
     * @param _rateList
     * @param _valueList
     * @return
     */
    public static <T> T getRandomValueByRate(List<Integer> _rateList, List<T> _valueList) {

        if (_rateList == null) {
        	Log.random.error("getRandomValueByRate. _rareList is null");
        }

        if (_valueList == null) {
        	Log.random.error("getRandomValueByRate. _valueList is null");
        }

        int index = getRandomIndexByRate(_rateList);
        if (-1 == index) {
            return null;
        }
        if (index >= _valueList.size()) {
            if (_valueList.isEmpty()) {
                return null;
            } else {
                return _valueList.get(_valueList.size() - 1);
            }
        }
        return _valueList.get(index);
    }
}
