#version 330 core

in vec2 v_texCoords;

out vec4 FragColor;

uniform vec3 iResolution;
uniform float iTime;
uniform float iTimeDelta;
uniform int iFrame;
uniform float iChannelTime[4];
uniform vec4 iMouse;
uniform vec4 iDate;
uniform float iSampleRate;
uniform vec3 iChannelResolution[4];
uniform sampler2D iChannel0;
uniform sampler2D iChannel1;
uniform sampler2D iChannel2;
uniform sampler2D iChannel3;

// SSAO (Screen Space AO) - by moranzcw - 2021
// Email: moranzcw@gmail.com
// License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.

#define PI 3.14159265359
#define AOradius 1.5
#define Samples 256.0

uniform float iGamma = 2.2;

// --------------------------------------
// oldschool rand() from Visual Studio
// --------------------------------------
int   seed = 1;
void  srand(int s ) { seed = s; }
int   rand(void)  { seed=seed*0x343fd+0x269ec3; return (seed>>16)&32767; }
float frand(void) { return float(rand())/32767.0; }
// --------------------------------------
// hash by Hugo Elias
// --------------------------------------
int hash( int n ) { n=(n<<13)^n; return n*(n*n*15731+789221)+1376312589; }

vec3 sphereVolumeRandPoint()
{
    vec3 p = vec3(frand(),frand(),frand()) * 2.0 - 1.0;
    while(length(p)>1.0)
    {
        p = vec3(frand(),frand(),frand()) * 2.0 - 1.0;
    }
    return p;
}

float depth(vec2 coord)
{
    vec2 uv = coord*vec2(iResolution.y/iResolution.x,1.0);
    vec3 encodedDepth = texture(iChannel0, uv).xyz;

    float depth;
    depth  = encodedDepth.b * 256.0 * 256.0;
    depth += encodedDepth.g * 256.0;
    depth += encodedDepth.r;

    return depth;
}

float SSAO(vec2 coord)
{
    float cd = depth(coord);
    float screenRadius = 0.5 * (AOradius / cd) / 0.53135;
    float li = 0.0;
    float count = 0.0;
    for(float i=0.0; i<Samples; i++)
    {
        vec3 p = sphereVolumeRandPoint() * frand();
        vec2 sp = vec2(coord.x + p.x * screenRadius, coord.y + p.y * screenRadius);
        float d = depth(sp);
        float at = pow(length(p)-1.0, 2.0);
        li += step(cd + p.z * AOradius, d) * at;
        count += at;
    }
    return li / count;
}

vec3 background(float yCoord)
{
    return vec3(1);
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    // init random seed
//    ivec2 q = ivec2(fragCoord);
//    srand( hash(q.x+hash(q.y+hash(1117*iFrame))));

    // coordinate
//    vec2 uv = fragCoord/iResolution.xy;
//    vec2 coord = fragCoord/iResolution.y;
//
//    float d = depth(coord);
//    vec3 ao = vec3(0.4) + step(d, 1e5-1.0) * vec3(0.8) * SSAO(coord);
////    vec3 color = mix(background(uv.y), ao, 1.0 - smoothstep(0.0, 0.99, d*d/1e3));;
//
//    vec3 color = pow(ao,vec3(1.0/iGamma)); // gamma
    fragColor = vec4(1.0);
}

void main()
{
    vec2 fragCoord = gl_FragCoord.xy;
    vec4 fragColor;
    mainImage(fragColor, fragCoord);
    FragColor = fragColor;
}