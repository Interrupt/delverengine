#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec4 u_FogColor;
uniform float u_time;
uniform float u_waterSpeed;

varying vec4 v_position;
varying vec4 v_color;
varying vec2 v_texCoords;
varying float v_fogFactor;
varying float v_eyeDistance;
varying float v_waveFactor;

vec4 tex_Color;

//
// Description : Array and textureless GLSL 2D simplex noise function.
//      Author : Ian McEwan, Ashima Arts.
//  Maintainer : ijm
//     Lastmod : 20110822 (ijm)
//     License : Copyright (C) 2011 Ashima Arts. All rights reserved.
//               Distributed under the MIT License. See LICENSE file.
//               https://github.com/ashima/webgl-noise
// 

vec3 mod289(vec3 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
  return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v)
  {
  const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
                      0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
                     -0.577350269189626,  // -1.0 + 2.0 * C.x
                      0.024390243902439); // 1.0 / 41.0
// First corner
  vec2 i  = floor(v + dot(v, C.yy) );
  vec2 x0 = v -   i + dot(i, C.xx);

// Other corners
  vec2 i1;
  //i1.x = step( x0.y, x0.x ); // x0.x > x0.y ? 1.0 : 0.0
  //i1.y = 1.0 - i1.x;
  i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
  // x0 = x0 - 0.0 + 0.0 * C.xx ;
  // x1 = x0 - i1 + 1.0 * C.xx ;
  // x2 = x0 - 1.0 + 2.0 * C.xx ;
  vec4 x12 = x0.xyxy + C.xxzz;
  x12.xy -= i1;

// Permutations
  i = mod289(i); // Avoid truncation effects in permutation
  vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
		+ i.x + vec3(0.0, i1.x, 1.0 ));

  vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
  m = m*m ;
  m = m*m ;

// Gradients: 41 points uniformly over a line, mapped onto a diamond.
// The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)

  vec3 x = 2.0 * fract(p * C.www) - 1.0;
  vec3 h = abs(x) - 0.5;
  vec3 ox = floor(x + 0.5);
  vec3 a0 = x - ox;

// Normalise gradients implicitly by scaling m
// Approximation of: m *= inversesqrt( a0*a0 + h*h );
  m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

// Compute final noise value at P
  vec3 g;
  g.x  = a0.x  * x0.x  + h.x  * x0.y;
  g.yz = a0.yz * x12.xz + h.yz * x12.yw;
  return 130.0 * dot(m, g);
}

void main() {
  vec4 color;
  vec3 under_color;
  vec2 under_scroll = vec2(0.0, 0.002 - (u_waterSpeed * 0.5));
  vec2 noisePoint = v_position.xz * 0.06;
  float waveFactor = (v_waveFactor * 1.25 - 0.5);

  noisePoint.x += u_time * 0.00005;
  noisePoint.y += u_time * 0.0001 + v_position.y * 0.02 + (u_time * u_waterSpeed * 0.04);
  float nc = snoise(noisePoint * 30.0 + waveFactor * 0.4);

  noisePoint.x = floor(nc * 0.4 - noisePoint.x * 512.0) / 512.0;
  noisePoint.y = floor(nc * 0.4 - noisePoint.y * 512.0) / 512.0;

  float pixel_nc = snoise(noisePoint * 45.0 + waveFactor * -0.1);
  pixel_nc = floor(pixel_nc * 1.75) / 1.75;

  noisePoint.x = v_texCoords.x;
  noisePoint.y = v_texCoords.y + nc * 0.03;

  tex_Color = texture2D( u_texture, noisePoint );
  tex_Color.rgb -= (waveFactor * pixel_nc) * 0.07;

    noisePoint.x = v_texCoords.x;
    noisePoint.y = v_texCoords.y + nc * -0.04;

  under_color = texture2D( u_texture, noisePoint + under_scroll * u_time ).rgb;
  under_color *= under_color;

  if((under_color.r * under_color.g * under_color.b) > 0.002)
  {
    tex_Color.rgb -= 0.025 + u_waterSpeed;
  }

  color = v_color * tex_Color;
  if(tex_Color.a < 0.1) discard;

  gl_FragColor = mix(u_FogColor, color, v_fogFactor);
}