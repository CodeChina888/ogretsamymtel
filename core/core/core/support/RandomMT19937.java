package core.support;

/**
 * 梅森随机数MT19937变种算法,本类内存占用约2M
 */
public class RandomMT19937 {
	/** 周期参数 624 **/
	private static final int N = 624;
	/** 周期参数 397 **/
	private static final int M = 397;
	/** most significant w-r bits 0x80000000L=2147483648 **/
	private static final long UPPER_MASK = 0x80000000L;
	/** least significant r bits 0x7fffffffL=2147483647 **/
	private static final long LOWER_MASK = 0x7fffffffL;	
	
	private static final long UN_MASK = 0xffffffffL;	
	
	/** 常数向量 a:0x9908b0dfL=2567483615 **/
	private static long[] MAGIC = {0x0L,0x9908b0dfL};

	private final static long  MAGIC_FACTOR1    = 1812433253L;
	private final static long  MAGIC_FACTOR2    = 1664525L;
	private final static long  MAGIC_FACTOR3    = 1566083941L;
	private final static long  MAGIC_MASK1      = 0x9d2c5680L;
	private final static long  MAGIC_MASK2      = 0xefc60000L;
	private final static long  MAGIC_SEED      = 19650218L;
	private final static long  DEFAULT_SEED    = 5489L;
	
	/** 状态向量数组 **/
	long[] mt = new long[N];
	/** mti==N+1 表示 mt[N] 未初始化 **/
	int mti = N + 1;
	
	public RandomMT19937(){
		setSeed(DEFAULT_SEED);
		
	}
	public RandomMT19937(long seed){
		setSeed(seed);
	}
	
	void setSeed(long s){
		mt[0]= s & UN_MASK;
	    for (mti=1; mti<N; mti++){
	        mt[mti] = (MAGIC_FACTOR1 * (mt[mti - 1] ^ (mt[mti - 1] >> 30)) + mti);
	        mt[mti] &= UN_MASK;
	    }
	}
//	/**
//	 * 初始化数组
//	 * @param init_key
//	 */
//	void init_by_array(long[] init_key)	{
//	    int i, j, k;
//	    setSeed(MAGIC_SEED);
//	    i = 1; j = 0;
//	    int key_length = init_key.length;
//	    k = (N > key_length ? N : key_length);
//	    for (; k>0; k--) 
//	    {
//	        mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >> 30)) * MAGIC_FACTOR2)) + init_key[j] + j; /* non linear */
//	        mt[i] &= UN_MASK; /* for WORDSIZE > 32 machines */
//	        i++; j++;
//	        if (i >= N) { mt[0] = mt[N - 1]; i = 1; }
//	        if (j >= key_length) j = 0;
//	    }
//	    for (k = N - 1; k>0; k--)
//	    {
//	        mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >> 30)) * MAGIC_FACTOR3)) - i; /* non linear */
//	        mt[i] &= UN_MASK; /* for WORDSIZE > 32 machines */
//	        i++;
//	        if (i >= N) { mt[0] = mt[N - 1]; i = 1; }
//	    }
//	 
//	    mt[0] = UPPER_MASK; /* MSB is 1; assuring non-zero initial array */
//	}
	/**
	 * 产生一个随机数在 [0,0xffffffff] 区间
	 * @return
	 */
	long genrand_int32(){
	    long y;	 
	    if (mti >= N) 
	    {
	        int kk;
	 
	        for (kk = 0; kk < N - M; kk++)
	        {
	            y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
	            mt[kk] = mt[kk + M] ^ (y >> 1) ^ MAGIC[(int) (y & 0x1L)];
	        }
	        for (; kk < N - 1; kk++)
	        {
	            y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
	            mt[kk] = mt[kk + (M - N)] ^ (y >> 1) ^ MAGIC[(int) (y & 0x1L)];
	        }
	        y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
	        mt[N - 1] = mt[M - 1] ^ (y >> 1) ^ MAGIC[(int) (y & 0x1L)];
	 
	        mti = 0;
	    }
	   
	    y = mt[mti++];
	 
	    /* Tempering */
	    y ^= (y >> 11);
	    y ^= (y << 7) & MAGIC_MASK1;
	    y ^= (y << 15) & MAGIC_MASK2;
	    y ^= (y >> 18);
	 
	    return y;
	}
	 
	/**
	 * 返回[0,1)之间的数
	 * @return
	 */
	public double nextDouble() {
		return genrand_int32()/(UN_MASK+0.5d);
	}

	/**
	 * 返回[0,range)之间的数
	 * @param range
	 * @return
	 */
	public int nextInt(int range) {
		return (int) (Math.floor(genrand_int32()/(UN_MASK+0.5d) * range));
	}
	 
//	public static void main(String[] params){
//	    System.out.println("梅森随机数MT19937变种算法示例:");
//	 
//	    RandomMT19937 mt = new RandomMT19937((long) (Math.random()*1000));
//	    int i;
//	    
//	    System.out.println("nextDouble() => 输出10个随机数double:");
//	    for (i = 0; i < 10; i++){
//	      System.out.println(mt.nextDouble());
//	    }
//	 
//	    System.out.println("nextInt() => 输出10个随机数int:");
//	    for (i = 0; i < 10; i++){
//	      System.out.println(mt.nextInt(10));
//	    }
//	}
	
}
