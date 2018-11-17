#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_texture1;

uniform float u_brightness;

uniform vec2 u_resolution;

vec4 blur9(sampler2D image, vec2 uv, vec2 resolution, vec2 direction);

void main()
{    
	// downsample the resolution
    float lowResolution = 240.0;
    float d = 1.0 / lowResolution;
	float ar = u_resolution.x / u_resolution.y;

	float pixelX = floor( v_texCoords.x / d );
	float u = pixelX * d;
	d = ar / lowResolution;

	float pixelY = floor( v_texCoords.y / d );
	float v = pixelY * d;

	vec4 color = texture2D(u_texture, vec2(u, v));

    // simple dither
	float ditherMod = mod(pixelY, 2.0);
	color.rgb -= mod(pixelX + ditherMod, 2.0) * 0.01;

	color.rg *= 16.0;
	color.b *= 8.0;
	color.rgb = floor(color.rgb);
	color.rg /= 16.0;
	color.b /= 8.0;
   
    gl_FragColor = color;
}