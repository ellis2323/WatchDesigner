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
#ifdef GL_ES
	float alpha = texture2D(iChannel0, vTexCoord0, 0.).a;
    lowp vec4 color =  vColor * alpha;
#else
	float alpha = texture2D(iChannel0, vTexCoord0, 0.).a;
    vec4 color =  vColor * alpha;
#endif
    alpha *= vColor.a;
    color.xyz *= alpha;
    gl_FragColor = color;
}

