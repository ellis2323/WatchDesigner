#version 120
#ifdef GL_ES
precision highp float;

uniform lowp sampler2D iChannel0;

varying mediump vec2 vTexCoord0;
varying lowp vec4 vColor;

#else

uniform sampler2D iChannel0;

varying vec2 vTexCoord0;
varying vec4 vColor;


#endif

void main()
{
    //gl_FragColor = texture2D(iChannel0, vTexCoord0) * vColor;
#ifdef GL_ES
    lowp vec4 color = texture2D(iChannel0, vTexCoord0, 0.) * vColor;
#else
    vec4 color = texture2D(iChannel0, vTexCoord0, 0.) * vColor;
#endif
    color.xyz *= vColor.a;
    gl_FragColor = color; 
}

