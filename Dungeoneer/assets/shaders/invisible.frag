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

float getOutlineMod(float offset_x, float offset_y);
float getOutline();
vec2 getPixelLocation();

void main() {
    vec4 color;
    vec4 tex_color;

    float fogFactor;
    fogFactor = v_fogFactor;

    tex_color = texture2D( u_texture, v_texCoords );
    color = v_color * tex_color;

    vec2 pixelLoc = getPixelLocation();

    float outlineMod = getOutline();
    float outline = outlineMod;
    outline *= (1.0 + sin(v_eyeDistance + u_time + pixelLoc.x * 200.0 + pixelLoc.y * 500.0)) * 0.4;
    outline += (outlineMod * (1.0 + sin(v_eyeDistance + u_time * 10.0 + pixelLoc.x * 300.0 + pixelLoc.y * 400.0))) * 0.1;
    outline *= (1.0 + cos(u_time + pixelLoc.x * 50.0));

    color.r += outline * 0.01;
    color.g += outline * 0.05;
    color.b += outline * 0.15;
    color.a *= clamp(outline, 0.2, 1.0);

    if(color.a <= 0.1) discard;
    
    gl_FragColor = mix(u_FogColor, color, fogFactor);
}

float getOutlineMod(float offset_x, float offset_y)
{
    vec2 shadow_texCoords = v_texCoords;
    shadow_texCoords.y += offset_y;
    shadow_texCoords.x += offset_x;

    vec4 tex_color = texture2D( u_texture, shadow_texCoords );
    return ceil(1.0 - tex_color.a);
}

float getOutline() {
    float outline = getOutlineMod(0.0, -u_tex_height);
    outline += getOutlineMod(0.0, u_tex_height);
    outline += getOutlineMod(u_tex_width, 0.0);
    outline += getOutlineMod(-u_tex_width, 0.0);
    return clamp(outline, 0.0, 2.0);
}

vec2 getPixelLocation() {
    vec2 loc;
    loc.x = floor(v_texCoords.x / u_tex_width);
    loc.y = floor(v_texCoords.y / u_tex_height);
    return loc;
}