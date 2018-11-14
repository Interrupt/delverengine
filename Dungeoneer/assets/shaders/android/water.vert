uniform mat4 u_projectionViewMatrix;
uniform float u_fogStart;
uniform float u_fogEnd;
uniform float u_time;
uniform float u_waterSpeed;

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

const float c_zerof = 0.0;
const float c_onef = 1.0;

vec4 vertexPositionInEye;

float calcFogFactor(float distanceToEye);

void main() {
    float wave = sin(u_time * 0.01 + a_position.x * 200.0 + a_position.z * 200.0);
    v_texCoords = a_texCoord0;
    
    vec4 newPosition = a_position;
    newPosition.y = newPosition.y + wave * 0.05 + 0.05;
    
    gl_Position = u_projectionViewMatrix * newPosition;
    
    v_texCoords.y = v_texCoords.y + sin(u_time * 0.01 + a_position.x) * 0.1;

    v_texCoords.y -= u_time * u_waterSpeed;
    
    v_color = a_color;
    
    for(int i=0; i < {{MAX_DYNAMIC_LIGHTS}}; i++)
    {
      if(u_LightColors[i].a != 0.0)
      {
  	    float lightDistance = length(u_LightPositions[i] - gl_Position.xyz) / u_LightColors[i].a;
        float diffuse = (1.0 / (1.0 + (lightDistance * lightDistance)));
        v_color += u_LightColors[i] * diffuse;
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
    
    // Exponential
	/*float f = exp((distanceToEye * u_fogDensity));
     return clamp(f, c_zerof, c_onef);*/
}