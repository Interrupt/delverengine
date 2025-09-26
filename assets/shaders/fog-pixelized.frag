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
	vec2 CoordOffset;
    vec4 color;
    float fogFactor;
    fogFactor = v_fogFactor;

    float Pixels = 512.0;
    float dx = 15.0 * (1.0 / Pixels);
    float dy = 10.0 * (1.0 / Pixels);

    CoordOffset = vec2(dx * floor(gl_FragCoord.x / dx),
                      dy * floor(gl_FragCoord.y / dy));
    
    color = v_color * texture2D( u_texture, v_texCoords );
    gl_FragColor = mix(u_FogColor, color, fogFactor);
    gl_FragColor.a *= clamp(((-v_eyeDistance - 1.5) * 0.4) * color.a, 0.0, color.a);

	float alphaFloor = floor(gl_FragColor.a * 20.0) / 20.0;
	gl_FragColor.a = 0.0;
    if (mod(CoordOffset.y, 8.0) > 4.0) {
    	if(mod(CoordOffset.x, 8.0) > 4.0) {
    		gl_FragColor.a = alphaFloor;
    	}
    }
    else {
    	if(mod(CoordOffset.x, 8.0) < 4.0) {
    		gl_FragColor.a = alphaFloor;
    	}
    }

    gl_FragColor.rgb += v_fogFactor * 0.05;
    gl_FragColor.rgb += u_FogColor.xyz * v_fogFactor * 0.35;

    if(color.a < 0.001) discard;
}