#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec4 u_FogColor;

varying vec4 v_color;
varying vec2 v_texCoords;
varying float v_fogFactor;
varying float v_eyeDistance;

uniform float u_time;

float getShadowMod()
{
	vec2 shadow_texCoords = v_texCoords;
    shadow_texCoords.y += 0.0078125;

    vec4 tex_color = texture2D( u_texture, shadow_texCoords );
    if(tex_color.a > v_color.a) {
      return 0.7;
    }

	return 1.0;
}

void main() {
    vec4 color;
    vec4 tex_color;

    float fogFactor;
    fogFactor = v_fogFactor;

    tex_color = texture2D( u_texture, v_texCoords );
    color = v_color * tex_color;

    if(tex_color.a > v_color.a) discard;
    if(tex_color.a < 0.01) discard;

    color.rgb *= getShadowMod();
    
    gl_FragColor = mix(u_FogColor, color, fogFactor);
}