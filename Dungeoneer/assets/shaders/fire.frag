#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec4 u_FogColor;

uniform float u_tex_width;
uniform float u_tex_height;

varying vec4 v_color;
varying vec2 v_texCoords;
varying float v_fogFactor;
varying float v_eyeDistance;

uniform float u_time;

void main() {
    vec4 color;
    vec4 tex_color;

    float fogFactor;
    fogFactor = v_fogFactor;

    tex_color = texture2D( u_texture, v_texCoords );
    color = v_color * tex_color;

    // get rid of strange flickering on transition areas
    tex_color.a = floor(tex_color.a * 1000.0) / 1000.0;

    if(tex_color.a <= v_color.a) discard;
    if(tex_color.a > v_color.a * 3.5) color.rgb *= 4.5;

    gl_FragColor = mix(u_FogColor, color, fogFactor);
}
