attribute vec4 a_position;
attribute vec2 a_texCoord0;
attribute vec4 a_color;

uniform mat4 u_projTrans;
uniform mat4 u_proj;
uniform mat4 u_trans;
uniform vec2 u_resolution;

varying vec4 v_color;
varying vec2 v_texCoords;

varying vec2 v_rgbNW;
varying vec2 v_rgbNE;
varying vec2 v_rgbSW;
varying vec2 v_rgbSE;
varying vec2 v_rgbM;

void texcoords(vec2 fragCoord, vec2 resolution,
			out vec2 v_rgbNW, out vec2 v_rgbNE,
			out vec2 v_rgbSW, out vec2 v_rgbSE,
			out vec2 v_rgbM);

void main()
{
	vec2 fragCoord = a_texCoord0 * u_resolution;
	texcoords(fragCoord, u_resolution, v_rgbNW, v_rgbNE, v_rgbSW, v_rgbSE, v_rgbM);

    v_color = a_color;
    v_texCoords = a_texCoord0;
    gl_Position =  u_projTrans * a_position;
}

void texcoords(vec2 fragCoord, vec2 resolution,
			out vec2 v_rgbNW, out vec2 v_rgbNE,
			out vec2 v_rgbSW, out vec2 v_rgbSE,
			out vec2 v_rgbM) {
	vec2 inverseVP = 1.0 / resolution.xy;
	v_rgbNW = (fragCoord + vec2(-1.0, -1.0)) * inverseVP;
	v_rgbNE = (fragCoord + vec2(1.0, -1.0)) * inverseVP;
	v_rgbSW = (fragCoord + vec2(-1.0, 1.0)) * inverseVP;
	v_rgbSE = (fragCoord + vec2(1.0, 1.0)) * inverseVP;
	v_rgbM = vec2(fragCoord * inverseVP);
}
