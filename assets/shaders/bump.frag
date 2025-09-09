#ifdef GL_ES
precision mediump float;
#endif
 
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_normals;
uniform vec3 light;
uniform vec3 ambientColor;
uniform float ambientIntensity;
uniform vec2 resolution;
uniform vec3 lightColor;
uniform bool useNormals;
uniform bool useShadow;
uniform vec3 attenuation;
uniform float strength;
uniform bool yInvert;
 
void main() {
        //sample color & normals from our textures
        vec4 color = texture2D(u_texture, v_texCoords.st);
        vec3 nColor = texture2D(u_normals, v_texCoords.st).rgb;
 
        //some bump map programs will need the Y value flipped..
        nColor.g = yInvert ? 1.0 - nColor.g : nColor.g;
 
        //this is for debugging purposes, allowing us to lower the intensity of our bump map
        vec3 nBase = vec3(0.5, 0.5, 1.0);
        nColor = mix(nBase, nColor, strength);
 
        //normals need to be converted to [-1.0, 1.0] range and normalized
        vec3 normal = normalize(nColor * 2.0 - 1.0);
 
        //here we do a simple distance calculation
        vec3 deltaPos = vec3( (light.xy - gl_FragCoord.xy) / resolution.xy, light.z );
 
        vec3 lightDir = normalize(deltaPos);
        float lambert = useNormals ? clamp(dot(normal, lightDir), 0.0, 1.0) : 1.0;
       
        //now let's get a nice little falloff
        float d = sqrt(dot(deltaPos, deltaPos));       
        float att = useShadow ? 1.0 / ( attenuation.x + (attenuation.y*d) + (attenuation.z*d*d) ) : 1.0;
       
        vec3 result = (ambientColor * ambientIntensity) + (lightColor.rgb * lambert) * att;
        result *= color.rgb;
       
        gl_FragColor = v_color * vec4(result, color.a);
}