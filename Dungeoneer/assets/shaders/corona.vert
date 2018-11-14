uniform mat4 u_projectionViewMatrix;
uniform float u_fogStart;
uniform float u_fogEnd;
uniform float u_drawDistance;

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
varying float v_distanceFactor;
varying float v_eyeDistance;

vec4 vertexPositionInEye;

const float c_zerof = 0.0;
const float c_onef = 1.0;

float calcFogFactor(float distanceToEye);
float calcDistanceFactor(float distanceToEye);

void main() {
  v_texCoords = a_texCoord0;
  gl_Position = u_projectionViewMatrix * a_position;

  v_color = a_color;
  v_color.rgb += u_AmbientColor.rgb;

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
  v_eyeDistance = -vertexPositionInEye.z;
  v_fogFactor = calcFogFactor(v_eyeDistance);
  v_distanceFactor = calcDistanceFactor(v_eyeDistance);
}

float calcFogFactor(float distanceToEye)
{
  float f = (u_fogEnd - -distanceToEye) / (u_fogEnd - u_fogStart);
	return clamp(f, 0.75, c_onef);
}

float calcDistanceFactor(float distanceToEye)
{
  float f = (u_drawDistance - -distanceToEye) / (u_drawDistance - u_fogEnd);
  return clamp(f, c_zerof, c_onef);
}