#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec4 u_FogColor;

varying vec4 v_color;
varying vec2 v_texCoords;
varying float v_fogFactor;
varying float v_eyeDistance;

vec4 tex_Color;

void main() {
  vec4 color;
  float fogFactor;
  fogFactor = v_fogFactor;
    

  tex_Color = texture2D( u_texture, v_texCoords );
  color = v_color * tex_Color;
  if(tex_Color.a < 0.1) discard;
    
  gl_FragColor = mix(u_FogColor, color, fogFactor);
}