uniform mat4 u_projectionViewMatrix;
uniform float u_fogStart;
uniform float u_fogEnd;
uniform float u_time;

uniform vec4 u_AmbientColor;
uniform vec4 u_FogColor;

uniform float u_tex_width;
uniform float u_tex_height;
uniform int u_sprite_columns;
uniform int u_sprite_rows;

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

const float c_zerof = 0.0;
const float c_onef = 1.0;
const float c_animateSpeed = 8.0;

vec4 vertexPositionInEye;

float calcFogFactor(float distanceToEye);

void main() {
  v_texCoords = a_texCoord0;
  gl_Position = u_projectionViewMatrix * a_position;
  v_color = a_color + u_AmbientColor;

  // Find out where in the sheet we are, and make a flipbook offset
  float texIndex = floor(mod(v_texCoords.x, 1.0) / u_tex_width);
  float indexOffset = mod(texIndex, float(u_sprite_columns));
  float animOffset = mod(floor(u_time * c_animateSpeed), float(u_sprite_columns));

  // Flip through the atlas on this row
  v_texCoords.x += animOffset * u_tex_width;

  // If we overrun our row, back up
  if(animOffset + indexOffset >= float(u_sprite_columns))
  {
    v_texCoords.x -= u_tex_width * float(u_sprite_columns);
  }

  if(u_UsedLights != 0) {
    for(int i=0; i < {{MAX_DYNAMIC_LIGHTS}}; i++)
    {
      if(u_LightColors[i].a != 0.0)
      {
  	    float lightDistance = length(u_LightPositions[i] - gl_Position.xyz) / u_LightColors[i].a;
        float diffuse = (1.0 / (1.0 + (lightDistance * lightDistance)));
        v_color += (u_LightColors[i] * diffuse) * a_color.a;
      }
    }
  }

  v_color.a = 1.0;

  vertexPositionInEye = gl_Position;
  v_fogFactor = calcFogFactor(-vertexPositionInEye.z - u_fogEnd * (1.0 - a_color.a));
  v_eyeDistance = -vertexPositionInEye.z;
}

float calcFogFactor(float distanceToEye)
{
    float f = (u_fogEnd - -distanceToEye) / (u_fogEnd - u_fogStart);
	return clamp(f, c_zerof, c_onef);
}