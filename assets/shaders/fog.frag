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
    gl_FragColor = mix(u_FogColor, color, fogFactor);
    gl_FragColor.a *= clamp(((-v_eyeDistance - 0.6) * 0.4) * color.a, 0.0, color.a);

    gl_FragColor.rgb += v_fogFactor * 0.05;
    gl_FragColor.rgb += u_FogColor.xyz * v_fogFactor * 0.35;

    if(color.a < 0.001) discard;
}