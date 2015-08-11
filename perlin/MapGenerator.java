import java.util.Date;
import java.util.Random;

public class MapGenerator {
	
	private static int DEFAULT_WIDTH=76;
	private static int DEFAULT_HEIGHT=20;
	private static int DEFAULT_OCTAVE_COUNT=4;
	private static int DEFAULT_LAND_PERCENTAGE=30;
	private static float DEFAULT_PERSISTENCE=0.4f;
	private static int MAX_TRIES_PER_WATER_LEVEL=100;

	public int[][] generateMap() throws Exception {
		return generateMap(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_OCTAVE_COUNT, DEFAULT_PERSISTENCE, DEFAULT_LAND_PERCENTAGE);
	}

	public int[][] generateMap(int w, int h, int o, float p, int t) throws Exception {
		
		float targetPercentage = (float) t / 100.0f;
		float bottomPercentage = targetPercentage - 0.05f;
		float topPercentage = targetPercentage + 0.05f;

		for (int l = 0; l <= 10; l++) {
			System.out.print(".");
			for (int attempt = 0; attempt < MAX_TRIES_PER_WATER_LEVEL; attempt++) {
				System.out.print(".");
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

				map = tidy(map,3);
				float landPercentage = getLandPercentage(map);
				if (landPercentage >= bottomPercentage && landPercentage <= topPercentage) {
					System.out.println("generated.\n\n");
					return map;
				}

			}

		}

		throw new Exception("Unable to generate map within given parameters");

	}

	private float[][] generateBaseMap(int w, int h, int o, float p) {
		float[][] noise = generateWhiteNoise(w,h);
		return generatePerlinNoise(noise, o, p);
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

	private int[][] tidy(int[][] map, int cycles) {

		for (int cycle=0; cycle<cycles; cycle++) {
			System.out.print(".");

			int[][] tidied = new int[map.length][map[0].length];
			for(int i=0; i<map.length; i++) {
	  			for(int j=0; j<map[0].length; j++) {
	    			tidied[i][j]=map[i][j];
	    		}
	    	}

			for(int i=0; i<map.length; i++) {
	  			for(int j=0; j<map[0].length; j++) {
	  				int neighbors = neighbors(map, i, j);
	  				if (neighbors < 2) {
	  					tidied[i][j]=0;
	  				}
	  			}
			}
			map = tidied;
		}
		return map;

	}

	private int neighbors(int[][] map, int i, int j) {
		int width = map.length-1;
		int height = map[0].length-1;
		int neighbors = 
			((i > 0 && j > 0) ? (map[i-1][j-1] > 0 ? 1 : 0) : 0)
		  + (i > 0 ? (map[i-1][j] > 0 ? 1 : 0) : 0)
		  + ((i > 0 && j < height) ? (map[i-1][j+1] > 0 ? 1 : 0): 0)
		  + (j > 0 ? (map[i][j-1] > 0 ? 1 : 0) : 0)
		  + (j < height ? (map[i][j+1] > 0 ? 1 : 0) : 0)
		  + ((i < width && j > 0) ? (map[i+1][j-1] > 0 ? 1 : 0) : 0)
		  + (i < width ? (map[i+1][j] > 0 ? 1 : 0) : 0)
		  + ((i < width && j < height) ? (map[i+1][j+1] > 0 ? 1 : 0) : 0);
		return neighbors;
	}

	private float getLandPercentage(int[][] map) {
		int totalArea = map.length * map[0].length;
		int landArea = 0;
		for(int i=0; i<map.length; i++) {
  			for(int j=0; j<map[0].length; j++) {
  				if (map[i][j] > 0) {
  					landArea++;
  				}
  			}
		}

		return (float) landArea / (float) totalArea;

	}

	private int wrap(int i, int i_max) {
	   return ((i % i_max) + i_max) % i_max;
	}

	public static void main(String args[]) {
		if (args.length != 0 && args.length != 1 && args.length != 5 && args.length != 6) {
			System.out.println("Usage:");
			System.out.println("\tjava MapGenerator (default values)");
			System.out.println("\tjava MapGenerator <width> <height> <smooth> <p> <land%> [empire]");
			System.out.println("\t\t<smooth> generally in the 2-5 range (default 4)");
			System.out.println("\t\t<p> 0-100, I have no idea how this works (default 40)");
			System.out.println("\t\t<land%> 0-100, target land percentage (within 5%) (default 30)");
			System.out.println("\t\t[empire] Empire mode!\n");
			return;
		}
		int w, h, o, l, p;

		System.out.print("Generating map.");
		MapGenerator generator = new MapGenerator();
		int flatMap[][];
		try {
			if (args.length < 5) {
				w = DEFAULT_WIDTH;
				h = DEFAULT_HEIGHT;
				flatMap = generator.generateMap();
			} else {
				w = Integer.parseInt(args[0]);
				h = Integer.parseInt(args[1]);
				o = Integer.parseInt(args[2]);
				p = Integer.parseInt(args[3]);
				l = Integer.parseInt(args[4]);
				flatMap = generator.generateMap(w,h,o,(float) p / 100,l);
			}

			for (int j = 0; j < h; j++) {
				for (int i = 0; i < w; i++) {
					if ((args.length == 1 && "empire".equalsIgnoreCase(args[0])) || (args.length == 6 && "empire".equalsIgnoreCase(args[5]))) {
						int highest = 0;

						for (int y = 0; y < h; y++) {
							for (int x = 0; x < w; x++) {
								if (flatMap[x][y] > highest) {
									highest = flatMap[x][y];
								}
							}
						}			
//						System.out.print(flatMap[i][j] == 0 ? '.' : flatMap[i][j] >= highest - 2 ? "^" : '+');
						System.out.print(flatMap[i][j] == 0 ? '.' : '+');
					} else {
						System.out.print(flatMap[i][j]);
					}
				}
				System.out.println();
			}
		} catch (Exception ex) {
			System.err.println("Unable to generate map within given parameters");
			ex.printStackTrace();
		}
	}

}