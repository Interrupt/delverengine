#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float u_time;

void main()
{    
    vec4 color = v_color * texture2D(u_texture, v_texCoords);
    if(color.a < 0.01) discard;

    float flashMod = sin(clamp(u_time, 0.0, 5.0) - (v_texCoords.x + (v_texCoords.y * 0.3))) + 1.0;

    if(flashMod < 1.99)
    {
    	flashMod = 1.0;
    }
    else
    {
    	flashMod = 1.1 + (flashMod - 1.99) * 200.0;
        flashMod -= clamp(sin((v_texCoords.x + (v_texCoords.y * 0.3)) * 70.0 + u_time * 2.0) * 1.25, 0.0, 1.25);
    }

    flashMod = clamp(flashMod, 1.0, 10.0);

    gl_FragColor = color;
    gl_FragColor *= (color * flashMod * color.a);
    gl_FragColor.a = color.a;
}