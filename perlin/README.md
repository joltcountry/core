Perlin noise-based map generator

<pre>
Usage:
	java MapGenerator [empire]
	java MapGenerator &lt;width&gt; &lt;height&gt; &lt;smooth&gt; &lt;p&gt; &lt;land percentage&gt; [empire]
		&lt;smooth&gt; generally in the 2-5 range, default 4
		&lt;p&gt; 0-100, I have no idea how this works, default 40
		&lt;land percentage&gt; percentage of land, within 5%, default 30
		[empire] Empire mode!
</pre>

Algorithm stolen from http://devmag.org.za/2009/04/25/perlin-noise/

