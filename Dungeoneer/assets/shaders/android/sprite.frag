#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform vec4 u_FogColor;

varying vec4 v_color;
varying vec2 v_texCoords;
varying float v_fogFactor;
varying float v_eyeDistance;

void main() {
    vec4 color;
    float fogFactor;
    fogFactor = v_fogFactor;
    
    color = v_color * texture2D( u_texture, v_texCoords );
    if(color.a < 0.9) discard;
    
    gl_FragColor = mix(u_FogColor, color, fogFactor);
}