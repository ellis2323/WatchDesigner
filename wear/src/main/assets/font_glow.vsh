uniform mat4 uMvp;
uniform float uThickness;

attribute vec3 aPosition;
attribute vec2 aTexCoord; 
attribute vec4 aColor;

varying vec4 vColor;
varying vec2 vTexCoord0;
varying float vVar;

void main() {
    vec4 position = vec4(aPosition.xyz, 1.);
    vColor = aColor;
    vTexCoord0 = aTexCoord;
    vVar = uThickness;
    gl_PointSize = 1.;
    gl_Position = uMvp * position;  
}
