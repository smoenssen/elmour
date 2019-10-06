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

    	if(distance > 0.075){
    	 		powDiff = 1.0 - pow(abs(diff*shockParams.x), shockParams.y);
       }
    	float diffTime = diff  * powDiff;
    	vec2 diffUV = normalize(texCoord - WaveCentre);

      // Perform the distortion and reduce the effect over time
      texCoord += ((diffUV * diffTime) / (CurrentTime * distance * -40.0));
	}
	gl_FragColor = texture2D(sceneTex, texCoord);

  // Blow out the color and reduce the effect over time
  gl_FragColor += (gl_FragColor * powDiff) / (CurrentTime * distance * 40.0);

}
