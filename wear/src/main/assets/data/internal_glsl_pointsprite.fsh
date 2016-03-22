#version 120
#ifdef GL_ES
precision highp float;
#endif 
uniform sampler2D iChannel0;

varying vec4 vColor;

void main() 
{
    gl_FragColor = texture2D(iChannel0, gl_PointCoord) * vColor;
}

