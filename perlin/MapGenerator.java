import java.util.Date;
import java.util.Random;

public class MapGenerator {
	
	private static int DEFAULT_WIDTH=60;
	private static int DEFAULT_HEIGHT=30;
	private static int DEFAULT_OCTAVE_COUNT=3;
	private static int DEFAULT_WATER_LEVEL=5;
	private static float DEFAULT_PERSISTENCE=0.5f;

	public int[][] generateMap() {
		return generateMap(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_OCTAVE_COUNT, DEFAULT_PERSISTENCE, DEFAULT_WATER_LEVEL);
	}

	public float[][] generateBaseMap(int w, int h, int o, float p) {
		float[][] noise = generateWhiteNoise(w,h);
		return generatePerlinNoise(noise, o, p);
	}

	public int[][] generateMap(int w, int h, int o, float p, int l) {
		int[][] map = new int[w][h];
		float[][] baseMap = generateBaseMap(w, h, o, p);
		int lowest = (int) Math.ceil(baseMap[0][0]*10);

		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				int altitude = (int) Math.ceil(baseMap[i][j]*10);
				if (altitude < lowest) {
					lowest = altitude;
				}
				map[i][j] = altitude;
			}
		}

		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				int altitude = map[i][j] - (lowest - 1);
				map[i][j] = (altitude <= l ? 0 : altitude - l);
			}
		}

		return map;
	}

	private float lerp(float x0, float x1, float alpha) {
   		return x0 * (1 - alpha) + alpha * x1;
	}

	private float[][] generateWhiteNoise(int width, int height)
	{
	    Random random = new Random();
	    //Random random = new Random(123l);
	    float[][] noise = new float[width][height];
	 
	    for (int i = 0; i < width; i++)
	    {
	        for (int j = 0; j < height; j++)
	        {
	            noise[i][j] = (float)random.nextDouble() % 1;
	        }
	    }
	 
	    return noise;
	}

	private float[][] generateSmoothNoise(float[][] baseNoise, int octave) {
	   int width = baseNoise.length;
	   int height = baseNoise[0].length;
	 
	   float[][] smoothNoise = new float[width][height];
	 
	   int samplePeriod = 1 << octave; // calculates 2 ^ k
	   float sampleFrequency = 1.0f / samplePeriod;
	 
	   for (int i = 0; i < width; i++)
	   {
	      //calculate the horizontal sampling indices
	      int sample_i0 = (i / samplePeriod) * samplePeriod;
	      int sample_i1 = (sample_i0 + samplePeriod) % width; //wrap around
	      float horizontal_blend = (i - sample_i0) * sampleFrequency;
	 
	      for (int j = 0; j < height; j++)
	      {
	         //calculate the vertical sampling indices
	         int sample_j0 = (j / samplePeriod) * samplePeriod;
	         int sample_j1 = (sample_j0 + samplePeriod) % height; //wrap around
	         float vertical_blend = (j - sample_j0) * sampleFrequency;
	 
	         //blend the top two corners
	         float top = lerp(baseNoise[sample_i0][sample_j0],
	            baseNoise[sample_i1][sample_j0], horizontal_blend);
	 
	         //blend the bottom two corners
	         float bottom = lerp(baseNoise[sample_i0][sample_j1],
	            baseNoise[sample_i1][sample_j1], horizontal_blend);
	 
	         //final blend
	         smoothNoise[i][j] = lerp(top, bottom, vertical_blend);
	      }

	   }
	 
	   return smoothNoise;

	}

	float[][] generatePerlinNoise(float[][] baseNoise, int octaveCount, float persistence)
	{
	   int width = baseNoise.length;
	   int height = baseNoise[0].length;
	 
	   float[][][] smoothNoise = new float[octaveCount][][]; //an array of 2D arrays containing
	 
	   //generate smooth noise
	   for (int i = 0; i < octaveCount; i++)
	   {
	       smoothNoise[i] = generateSmoothNoise(baseNoise, i);
	   }
	 
	    float[][] perlinNoise = new float[width][height];
	    float amplitude = 1.0f;
	    float totalAmplitude = 0.0f;
	 
	    //blend noise together
	    for (int octave = octaveCount - 1; octave >= 0; octave--)
	    {
	       amplitude *= persistence;
	       totalAmplitude += amplitude;
	 
	       for (int i = 0; i < width; i++)
	       {
	          for (int j = 0; j < height; j++)
	          {
	             perlinNoise[i][j] += smoothNoise[octave][i][j] * amplitude;
	          }
	       }
	    }
	 
	   //normalisation
	   for (int i = 0; i < width; i++)
	   {
	      for (int j = 0; j < height; j++)
	      {
	         perlinNoise[i][j] /= totalAmplitude;
	      }
	   }
	 
	   return perlinNoise;

	}

	public static void main(String args[]) {
		if (args.length != 0 && args.length != 5) {
			System.out.println("Usage:");
			System.out.println("\tjava MapGenerator (default values)");
			System.out.println("\tjava MapGenerator <width> <height> <smooth> <p> <waterlevel>");
			System.out.println("\t\t<smooth> generally in the 3-7 range");
			System.out.println("\t\t<p> 0-9, I have no idea how this works");
			System.out.println("\t\t<waterlevel> 0-9, 0 = no water, takes experimentation\n");
			return;
		}
		int w, h, o, l, p;

		System.out.println("Generating map...");
		MapGenerator generator = new MapGenerator();
		int flatMap[][];
		if (args.length == 0) {
			w = DEFAULT_WIDTH;
			h = DEFAULT_HEIGHT;
			flatMap = generator.generateMap();
		} else {
			w = Integer.parseInt(args[0]);
			h = Integer.parseInt(args[1]);
			o = Integer.parseInt(args[2]);
			p = Integer.parseInt(args[3]);
			l = Integer.parseInt(args[4]);
			flatMap = generator.generateMap(w,h,o,(float) p / 10,l);
		}
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				System.out.print(flatMap[i][j]);
			}
			System.out.println();
		}
	}

}