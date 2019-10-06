#ifdef GL_ES
precision highp float;
#endif

//10 (will look bad if it is anything else (sharpness of the edges)), Strength of Shockwave (Maybe dissipation), Overall Size

//CurrentTime = larger # means greater radius
//powDiff = 0.0 ; If it is not, it will grab pixels that are not in the center
//diff = dissipation
//diffUV = what dirrection the shift is in (i.e. diagonally)


uniform sampler2D sceneTex; // 0
uniform vec2 center; // Mouse position
uniform float time; // effect elapsed time
//uniform vec3 shockParams; // 10.0, 0.8, 0.1

varying vec2 v_texCoords;

vec2 hash( vec2 x )
{
    const vec2 k = vec2( 0.3183099, 0.3678794 );
    x = x*k + k.yx;
    return -1.0 + 2.0*fract( 16.0 * k*fract( x.x*x.y*(x.x+x.y)) );
}

float noise( in vec2 p )
{
    vec2 i = floor( p );
    vec2 f = fract( p );

	vec2 u = f*f*(3.0-2.0*f);

    return mix( mix( dot( hash( i + vec2(0.0,0.0) ), f - vec2(0.0,0.0) ),
                     dot( hash( i + vec2(1.0,0.0) ), f - vec2(1.0,0.0) ), u.x),
                mix( dot( hash( i + vec2(0.0,1.0) ), f - vec2(0.0,1.0) ),
                     dot( hash( i + vec2(1.0,1.0) ), f - vec2(1.0,1.0) ), u.x), u.y);
}

void main()
{
  vec2 iResolution = vec2(1, 0.75);
	vec3 shockParams = vec3(10.0, 0.8, 0.1);

  float ratio = iResolution.y/iResolution.x;
  vec2 WaveCentre = center;
  //WaveCentre.y *= ratio;

  // get pixel coordinates
  vec2 texCoord = v_texCoords.xy / iResolution.xy;
  texCoord.y *= ratio;

  float offset = (time- floor(time))/time;
	float CurrentTime = (time)*(offset);

	//get distance from center
	float distance = distance(texCoord, WaveCentre);

  float powDiff = 0.0;

	if ( (distance <= (CurrentTime + shockParams.z)) && (distance >= (CurrentTime - shockParams.z)) ) {
    	float diff = (distance - CurrentTime);

    	if(distance > 0.05){
    	 		powDiff = 1.0 - pow(abs(diff*shockParams.x), shockParams.y);

          //////////////////////////////////////////////
          // Normalized pixel coordinates (from 0 to 1)
          vec2 uv = texCoord/iResolution.xy; // 0 <> 1

          uv -= .5; // -0.5 <> 0.5
          uv.x *= iResolution.x/iResolution.y;

          float noiseboi = 0.0;
          noiseboi = noise(16.0 * uv); //changes frequency

          vec2 p = vec2(0);
          float r = (mod(time, 2.));
          float dissolve = r;
          noiseboi *= dissolve;
          noiseboi *= texCoord.x + texCoord.y;

          float diffTime = diff  * powDiff;
          vec2 diffUV = normalize(v_texCoords - center);

          texCoord = v_texCoords + ((diffUV * diffTime)/(CurrentTime * distance * -40.0));

          texCoord += noiseboi;
          ///////////////////////////////////////////////
       }

    	float diffTime = diff  * powDiff;
    	vec2 diffUV = normalize(texCoord - WaveCentre);

      // Perform the distortion and reduce the effect over time
      texCoord += ((diffUV * diffTime) / (CurrentTime * distance * 40.0));
	}
	gl_FragColor = texture2D(sceneTex, texCoord);

  // Blow out the color and reduce the effect over time
  gl_FragColor += (gl_FragColor * powDiff) / (CurrentTime * distance * 40.0);

}
