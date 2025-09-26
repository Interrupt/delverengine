uniform mat4 u_projectionViewMatrix;
uniform float u_fogStart;
uniform float u_fogEnd;

uniform vec4 u_AmbientColor;
uniform vec4 u_FogColor;

uniform float u_time;
uniform float u_tex_width;
uniform float u_tex_height;
uniform int u_sprite_columns;
uniform int u_sprite_rows;

uniform vec4 u_LightColors[{{MAX_DYNAMIC_LIGHTS}}];
uniform vec3 u_LightPositions[{{MAX_DYNAMIC_LIGHTS}}];
uniform int u_UsedLights;

uniform float u_wave_offset;

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
float calcAttachment();

void main() {
  v_texCoords = a_texCoord0;

  vec4 newPos = a_position;

  float smallMovement = sin(u_time * 3.0 + a_position.x * 2.0) * 0.5;
  float bigMovement = sin(u_time * 1.2 + a_position.x * 2.0) * 0.5 + 0.5;
  float gust = sin(u_time * 0.8 + a_position.x * 1.0) * 0.5 + 0.5;
  gust *= gust;

  float swayMod = clamp(calcAttachment(), -1.0, 1.0);

  newPos.x += (gust * 0.1 + smallMovement * 0.08 + bigMovement * 0.1) * swayMod;
  newPos.z += (gust * 0.05 + smallMovement * 0.05 + bigMovement * 0.1) * swayMod;

  gl_Position = u_projectionViewMatrix * newPos;

  v_color = a_color + (u_AmbientColor * a_color.a);

  for(int i=0; i < {{MAX_DYNAMIC_LIGHTS}}; i++)
  {
    if(v_color.a != 0.0 && u_LightColors[i].a != 0.0)
    {
  	  float lightDistance = length(u_LightPositions[i] - gl_Position.xyz) / u_LightColors[i].a;
      float diffuse = (1.0 / (1.0 + (lightDistance * lightDistance)));
      v_color += u_LightColors[i] * diffuse;
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
}

float calcAttachment() {
  float mod_height = 1.0 / float(u_sprite_rows);
  return (1.0 - u_wave_offset) - mod(v_texCoords.y, mod_height) / mod_height;
}