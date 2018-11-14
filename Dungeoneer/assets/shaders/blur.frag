#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_brightness;
uniform vec2 u_resolution;
uniform vec2 u_direction;

vec4 blur9(sampler2D image, vec2 uv, vec2 resolution, vec2 direction);

void main()
{
    vec4 blur = blur9(u_texture, v_texCoords, u_resolution, u_direction);
    blur.a = 1.0;
    gl_FragColor = blur;
}

vec4 blur9(sampler2D image, vec2 uv, vec2 resolution, vec2 direction) {
  vec4 color = vec4(0.0);
  vec2 off1 = vec2(1.3846153846) * direction;
  vec2 off2 = vec2(3.2307692308) * direction;
  color += texture2D(image, uv) * 0.2270270270;
  color += texture2D(image, uv + (off1 / resolution)) * 0.3162162162;
  color += texture2D(image, uv - (off1 / resolution)) * 0.3162162162;
  color += texture2D(image, uv + (off2 / resolution)) * 0.0702702703;
  color += texture2D(image, uv - (off2 / resolution)) * 0.0702702703;

  return color;
}