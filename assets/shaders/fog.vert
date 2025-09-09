uniform mat4 u_projectionViewMatrix;
uniform float u_fogStart;
uniform float u_fogEnd;
uniform float u_time;

uniform vec4 u_AmbientColor;
uniform vec4 u_FogColor;

uniform vec4 u_LightColors[{{MAX_DYNAMIC_LIGHTS}}];
uniform vec3 u_LightPositions[{{MAX_DYNAMIC_LIGHTS}}];
uniform int u_UsedLights;

attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

varying vec2 v_texCoords;
varying vec4 v_color;

varying float v_fogFactor;
varying float v_eyeDistance;

vec4 vertexPositionInEye;

const float c_zerof = 0.0;
const float c_onef = 1.0;

float calcFogFactor(float distanceToEye);

void main() {
  v_texCoords = a_texCoord0;

  float smallMovement = sin(u_time * 3.0 + a_position.x * 2.0) * 0.5;
  float bigMovement = sin(u_time * 1.2 + a_position.x * 2.0) * 0.5 + 0.5;
  float gust = sin(u_time * 0.8 + a_position.x * 1.0) * 0.5 + 0.5;
  gust *= gust;

  gl_Position = u_projectionViewMatrix * a_position;

  v_color = a_color;
  v_color.rgb += u_AmbientColor.rgb;
  v_color.a -= (gust * 0.1 + smallMovement * 0.08 + bigMovement * 0.1) * (v_color.a * 1.2);

  if(u_UsedLights != 0) {
    for(int i=0; i < {{MAX_DYNAMIC_LIGHTS}}; i++)
    {
      if(u_LightColors[i].a != 0.0)
      {
        float lightDistance = length(u_LightPositions[i] - gl_Position.xyz) / u_LightColors[i].a;
        float diffuse = (1.0 / (1.0 + (lightDistance * lightDistance)));
        v_color.rgb += u_LightColors[i].rgb * diffuse;
      }
    }
  }
    
  vertexPositionInEye = gl_Position;
  v_fogFactor = calcFogFactor(-vertexPositionInEye.z);
  v_eyeDistance = -vertexPositionInEye.z;
}

float calcFogFactor(float distanceToEye)
{
  float f = (u_fogEnd - -distanceToEye) / (u_fogEnd - u_fogStart);
	return clamp(f, c_zerof, c_onef);
}