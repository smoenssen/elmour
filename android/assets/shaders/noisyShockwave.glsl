#ifdef GL_ES
precision highp float;
//precision mediump float;
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

//***************************************************


vec3 Circle(vec2 uv, vec2 p, float r, float blur, vec3 color) {

    float d = length(uv-p);
    float c = smoothstep(r, r-blur, d);
    color = vec3(color*c);
    return color;
}

void main()
{
    vec2 fragCoord = v_texCoords;
    vec2 iResolution = vec2(1,1);

    /////////////////
    vec3 shockParams = vec3(10.0, 0.8, 0.1);

    float offset = (time - floor(time))/time;
    float CurrentTime = (time)*(offset);


    //get distance from center
    float distance = distance(v_texCoords, center);

    if ((distance <= (CurrentTime + shockParams.z)) && (distance >= (CurrentTime - shockParams.z)) )
    {
        float diff = (distance - CurrentTime);

        float powDiff = 0.0;
        float minRadius = 0.075;

        if(distance > minRadius){
          powDiff = 1.0 - pow(abs(diff*shockParams.x), shockParams.y);

          // Normalized pixel coordinates (from 0 to 1)
          vec2 uv = fragCoord/iResolution.xy; // 0 <> 1

          uv -= .5; // -0.5 <> 0.5
          uv.x *= iResolution.x/iResolution.y;

          float noiseboi = 0.0;
          noiseboi = noise(32.0 * uv); //changes frequency

          vec2 p = vec2(0);
          float r = (mod(time, 2.));
          float dissolve = r;
          noiseboi *= dissolve;
          noiseboi *= fragCoord.x + fragCoord.y;

          float diffTime = diff  * powDiff;
          vec2 diffUV = normalize(v_texCoords - center);

          fragCoord = v_texCoords + ((diffUV * diffTime)/(CurrentTime * distance * -40.0));

          fragCoord += noiseboi;
        }
        else {
          //float TimeTill = 0.3;
          //float percent = CurrentTime / (TimeTill);
          //diff = (distance - CurrentTime);
          //powDiff = (1.0 - pow(abs(diff*shockParams.x), shockParams.y)) * percent;
          //powDiff = distance;

          float diffTime = diff  * powDiff;
          vec2 diffUV = normalize(v_texCoords - center);
          //Perform the distortion and reduce the effect over time
          fragCoord = v_texCoords + ((diffUV * diffTime)/(CurrentTime * distance * -40.0)); // negative 40 helped reverse the wave


        }
    }

    gl_FragColor = texture2D(sceneTex, fragCoord);
}
