uniform mat4 u_projectionViewMatrix;
uniform float u_fogStart;
uniform float u_fogEnd;
uniform float u_time;

uniform vec4 u_FogColor;

uniform vec4 u_LightColors[{{MAX_DYNAMIC_LIGHTS}}];
uniform vec3 u_LightPositions[{{MAX_DYNAMIC_LIGHTS}}];
uniform int u_UsedLights;
uniform float u_waveMod;

attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

varying vec4 v_position;
varying vec2 v_texCoords;
varying vec4 v_color;
varying float v_waveFactor;

varying float v_fogFactor;
varying float v_eyeDistance;

const float c_zerof = 0.0;
const float c_onef = 1.0;

vec4 vertexPositionInEye;

float calcFogFactor(float distanceToEye);
float fluidWave(float steepness, float amplitude, float w, float phase_const, vec2 direction);

void main() {
    float wave = sin(u_time * 0.01 + a_position.x * 200.0 + a_position.z * 200.0);
    v_texCoords = a_texCoord0;
    v_position = a_position;
    
    vec4 newPosition = a_position;

    v_waveFactor = fluidWave(8.0, 0.055, 1.0, 0.05, vec2(0.5, 0.5));
    v_waveFactor += fluidWave(8.0, 0.015, 1.0, 0.1, vec2(0.2, -0.8));

    newPosition.y += v_waveFactor * 0.1;
    newPosition.y += 0.055;

    v_texCoords.y -= (wave * 0.04 + 0.04) * u_waveMod;
    
    gl_Position = u_projectionViewMatrix * newPosition;
    
    v_color = a_color;
    
    if(u_UsedLights != 0) {
      for(int i=0; i < {{MAX_DYNAMIC_LIGHTS}}; i++)
      {
        if(u_LightColors[i].a != 0.0)
        {
  	      float lightDistance = length(u_LightPositions[i] - gl_Position.xyz) / u_LightColors[i].a;
          float diffuse = (1.0 / (1.0 + (lightDistance * lightDistance)));
          v_color += u_LightColors[i] * diffuse;
        }
      }
    }

    v_color.a = 1.0;
    
    vertexPositionInEye = gl_Position;
    v_fogFactor = calcFogFactor(-vertexPositionInEye.z);
    v_eyeDistance = -vertexPositionInEye.z;
}

float calcFogFactor(float distanceToEye)
{
    float f = (u_fogEnd - -distanceToEye) / (u_fogEnd - u_fogStart);
	return clamp(f, c_zerof, c_onef);
    
    // Exponential
	/*float f = exp((distanceToEye * u_fogDensity));
     return clamp(f, c_zerof, c_onef);*/
}

float fluidWave(float steepness, float amplitude, float w, float phase_const, vec2 direction)
{
  return ((steepness * amplitude) * direction.y * cos(w * dot(direction, (a_position.xz * 4.0)) + (phase_const * u_time)));
}