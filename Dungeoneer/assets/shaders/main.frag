#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec4 u_FogColor;
uniform float u_time;

varying vec4 v_color;
varying vec2 v_texCoords;
varying float v_fogFactor;
varying float v_eyeDistance;

const float c_epsilon = 1.0 / 256.0;

void main() {
    vec4 tex_Color = texture2D(u_texture, v_texCoords);
    if(tex_Color.a < c_epsilon) discard;

    // Alpha channel drives emissive/fullbrite
    vec4 fullbrite = tex_Color / (tex_Color.a);
    vec4 color = mix(fullbrite, tex_Color * v_color, tex_Color.a);

    gl_FragColor = mix(u_FogColor, color, v_fogFactor);
}